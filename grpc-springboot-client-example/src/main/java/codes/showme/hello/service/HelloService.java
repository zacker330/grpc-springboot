package codes.showme.hello.service;

import codes.showme.examples.GreeterGrpc;
import codes.showme.examples.GreeterOuterClass;
import codes.showme.hello.consul.ConsulNameResolver;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class HelloService implements InitializingBean {

    public static final String GRPC_SERVER_NAME = "grpc-server";

    private static final Logger logger = LoggerFactory.getLogger(HelloService.class);

    private ManagedChannel channel;



    public String hello(String aString) {
        String result = "";
        try {
            result = GreeterGrpc.newFutureStub(getChannel())
                    .sayHello(GreeterOuterClass.HelloRequest.newBuilder().setName(aString == null ? "default" : aString).build())
                    .get().getMessage();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return result;
    }

    private ManagedChannel getChannel(){
        return  ManagedChannelBuilder.forTarget("http://127.0.0.1:8500")
                .usePlaintext(true)
                .nameResolverFactory(new ConsulNameResolver.ConsulNameResolverProvider(GRPC_SERVER_NAME,5,false, Arrays.asList()))
                .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
                .usePlaintext(true)
                .build();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        channel = getChannel();
        logger.info("init " + this.getClass());
    }

}
