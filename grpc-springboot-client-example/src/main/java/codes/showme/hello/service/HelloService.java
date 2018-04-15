package codes.showme.hello.service;

import codes.showme.examples.GreeterGrpc;
import codes.showme.examples.GreeterOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class HelloService implements InitializingBean {

    public static final String GRPC_SERVER_NAME = "grpc-server";

    private static final Logger logger = LoggerFactory.getLogger(HelloService.class);


    @Resource
    private DiscoveryClient discoveryClient;

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

    public Optional<ServiceInstance> service() {
        return discoveryClient.getInstances(GRPC_SERVER_NAME)
                .stream().findFirst();
    }

    private ManagedChannel getChannel(){
        if (service().isPresent()) {
            final ServiceInstance serviceInstance = service().get();
            final URI uri = serviceInstance.getUri();
            final String helloServiceHost = uri.getHost();
            int gRpcPort=Integer.parseInt(serviceInstance.getMetadata().get("grpc.port"));
            return  ManagedChannelBuilder.forAddress(helloServiceHost, gRpcPort).usePlaintext(true)
                    .build();
        } else {
            // TODO
            return null;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {


    }

}
