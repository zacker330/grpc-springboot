package codes.showme.hello.service;

import codes.showme.examples.GreeterGrpc;
import codes.showme.examples.GreeterOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class HelloService implements InitializingBean{

    private ManagedChannel channel;

    @Value("${helloServiceHost:localhost}")
    private String helloServiceHost;

    @Value("${helloServicePort:6565}")
    private int helloServicePort;

    public String hello(String aString) {
        String result = "";
        try {
            result = GreeterGrpc.newFutureStub(channel)
                    .sayHello(GreeterOuterClass.HelloRequest.newBuilder().setName(aString == null ? "default":aString).build())
                    .get().getMessage();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }




    @Override
    public void afterPropertiesSet() throws Exception {
        channel = ManagedChannelBuilder.forAddress(helloServiceHost, helloServicePort).usePlaintext(true)
                .build();
    }

    public void setHelloServiceHost(String helloServiceHost) {
        this.helloServiceHost = helloServiceHost;
    }

    public void setHelloServicePort(int helloServicePort) {
        this.helloServicePort = helloServicePort;
    }
}
