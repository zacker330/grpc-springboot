package codes.showme.hello.consul;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ConsulNameResolver extends NameResolver {

    private static Logger log = LoggerFactory.getLogger(ConsulNameResolver.class);

    private static final int DEFAULT_PAUSE_IN_SECONDS = 5;

    private URI uri;
    private String serviceName;
    private int pauseInSeconds;
    private boolean ignoreConsul;
    private List<String> hostPorts;

    private Listener listener;

    private List<ServiceDiscovery.ServiceNode> nodes;

    private ConnectionCheckTimer connectionCheckTimer;

    public ConsulNameResolver(URI uri, String serviceName, int pauseInSeconds, boolean ignoreConsul, List<String> hostPorts) {
        this.uri = uri;
        this.serviceName = serviceName;
        this.pauseInSeconds = pauseInSeconds;
        this.ignoreConsul = ignoreConsul;
        this.hostPorts = hostPorts;

        // run connection check timer.
        this.connectionCheckTimer = new ConnectionCheckTimer(this, this.pauseInSeconds);
        this.connectionCheckTimer.runTimer();
    }

    @Override
    public String getServiceAuthority() {
        return this.uri.getAuthority();
    }

    @Override
    public void start(Listener listener) {
        this.listener = listener;

        loadServiceNodes();
    }

    private void loadServiceNodes() {
        List<EquivalentAddressGroup> addrs = new ArrayList<>();

        if (!this.ignoreConsul) {
            String consulHost = uri.getHost();
            int consulPort = uri.getPort();

            nodes = getServiceNodes(serviceName, consulHost, consulPort);
            if (nodes == null || nodes.size() == 0) {
                log.info("there is no node info for serviceName: [{}]...", serviceName);
                return;
            }

            for (ServiceDiscovery.ServiceNode node : nodes) {
                String host = node.getHost();
                int port = node.getPort();
                log.info("serviceName: [" + serviceName + "], host: [" + host + "], port: [" + port + "]");

                List<SocketAddress> sockaddrsList = new ArrayList<SocketAddress>();
                sockaddrsList.add(new InetSocketAddress(host, port));
                addrs.add(new EquivalentAddressGroup(sockaddrsList));
            }
        } else {
            nodes = new ArrayList<>();
            for (String hostPort : this.hostPorts) {
                String[] tokens = hostPort.split(":");

                String host = tokens[0];
                int port = Integer.valueOf(tokens[1]);
                log.info("static host: [" + host + "], port: [" + port + "]");

                nodes.add(new ServiceDiscovery.ServiceNode("", host, port));

                List<SocketAddress> sockaddrsList = new ArrayList<SocketAddress>();
                sockaddrsList.add(new InetSocketAddress(host, port));
                addrs.add(new EquivalentAddressGroup(sockaddrsList));
            }
        }

        if (addrs.size() > 0) {
            this.listener.onAddresses(addrs, Attributes.EMPTY);
        }
    }

    public List<ServiceDiscovery.ServiceNode> getNodes() {
        return this.nodes;
    }

    private List<ServiceDiscovery.ServiceNode> getServiceNodes(String serviceName, String consulHost, int consulPort) {

        ServiceDiscovery serviceDiscovery = ConsulServiceDiscovery.singleton(consulHost, consulPort);
        return serviceDiscovery.getHealthServices(serviceName);
    }

    @Override
    public void shutdown() {

    }

    private static class ConnectionCheckTimer {
        private ConnectionCheckTimerTask timerTask;
        private int delay = 1000;
        private int pauseInSeconds;
        private Timer timer;
        private ConsulNameResolver consulNameResolver;

        public ConnectionCheckTimer(ConsulNameResolver consulNameResolver, int pauseInSeconds) {
            this.consulNameResolver = consulNameResolver;
            this.pauseInSeconds = pauseInSeconds;

            this.timerTask = new ConnectionCheckTimerTask(this.consulNameResolver);
            this.timer = new Timer();
        }

        public void runTimer() {
            this.timer.scheduleAtFixedRate(this.timerTask, delay, this.pauseInSeconds * 1000);
        }

        public void reset() {
            this.timerTask.cancel();
            this.timer.purge();
            this.timerTask = new ConnectionCheckTimerTask(consulNameResolver);
        }
    }

    private static class ConnectionCheckTimerTask extends TimerTask {
        private ConsulNameResolver consulNameResolver;

        public ConnectionCheckTimerTask(ConsulNameResolver consulNameResolver) {
            this.consulNameResolver = consulNameResolver;
        }

        @Override
        public void run() {
            List<ServiceDiscovery.ServiceNode> nodes = consulNameResolver.getNodes();
            if (nodes != null) {
                for (ServiceDiscovery.ServiceNode node : nodes) {
                    String host = node.getHost();
                    int port = node.getPort();
                    try {
                        Socket socketClient = new Socket(host, port);
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        log.info("service nodes being reloaded...");

                        this.consulNameResolver.loadServiceNodes();
                        break;
                    }
                }
            } else {
                log.info("no service nodes...");
            }
        }
    }

    public static class ConsulNameResolverProvider extends NameResolverProvider {

        private String serviceName;
        private int pauseInSeconds;
        private boolean ignoreConsul;
        private List<String> hostPorts;

        public ConsulNameResolverProvider(String serviceName, int pauseInSeconds, boolean ignoreConsul, List<String> hostPorts) {
            this.serviceName = serviceName;
            this.pauseInSeconds = pauseInSeconds;
            this.ignoreConsul = ignoreConsul;
            this.hostPorts = hostPorts;
        }

        @Override
        protected boolean isAvailable() {
            return true;
        }

        @Override
        protected int priority() {
            return 5;
        }

        @Nullable
        @Override
        public NameResolver newNameResolver(URI uri, Attributes attributes) {
            log.info("NameResolver url: " + uri);
            return new ConsulNameResolver(uri, serviceName, pauseInSeconds, this.ignoreConsul, this.hostPorts);
        }

        @Override
        public String getDefaultScheme() {
            return "http";
        }
    }
}