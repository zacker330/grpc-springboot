import codes.showme.examples.GreeterGrpc;
import codes.showme.examples.GreeterOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

//@Ignore
public class AppClient {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext(true)
                .build();
        for (int i = 0; i < 100; i++) {
            System.out.println(GreeterGrpc.newFutureStub(channel)
                    .sayHello(GreeterOuterClass.HelloRequest.newBuilder().setName("life is short").build())
                    .get().getMessage());
        }

    }

}
