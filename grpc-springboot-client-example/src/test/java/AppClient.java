import codes.showme.examples.GreeterGrpc;
import codes.showme.examples.GreeterOuterClass;
import codes.showme.hello.consul.ConsulNameResolver;
import grpc.health.v1.HealthGrpc;
import grpc.health.v1.HealthOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

//@Ignore
public class AppClient {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext(true)
//                .build();
//        for (int i = 0; i < 100; i++) {
//            System.out.println(GreeterGrpc.newFutureStub(channel)
//                    .sayHello(GreeterOuterClass.HelloRequest.newBuilder().setName("life is short").build())
//                    .get().getMessage());
//        }
//


    }

    @Test
    public void health() throws Exception {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 6565)
                .usePlaintext(true)
                .build();


        System.out.println(HealthGrpc.newBlockingStub(channel).check(HealthOuterClass.HealthCheckRequest.newBuilder().build()).getStatus());

    }

    @Test
    public void name() throws Exception {

        ManagedChannel channel = ManagedChannelBuilder
                .forTarget("consul://127.0.0.1:8500")
                .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
                .nameResolverFactory(
                        new ConsulNameResolver.ConsulNameResolverProvider("grpc-server", 5, true, Arrays.asList("localhost:8092")))
                .usePlaintext(true)
                .build();


        GreeterGrpc.newFutureStub(channel)
                .sayHello(GreeterOuterClass.HelloRequest.newBuilder().setName("life is short").build())
                .get().getMessage();


    }

    @Test
    public void greet() throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext(true)
                .build();
        System.out.println(GreeterGrpc.newFutureStub(channel)
                .sayHello(GreeterOuterClass.HelloRequest.newBuilder().setName("life is short").build())
                .get().getMessage());

    }
}
