import codes.showme.examples.GreeterGrpc;
import codes.showme.examples.GreeterOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Test;

public class AppClient {
    @Test
    public void name() throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext(true)
                .build();
        System.out.println(GreeterGrpc.newFutureStub(channel)
                .sayHello(GreeterOuterClass.HelloRequest.newBuilder().setName("life is short").build())
                .get().getMessage());

    }
}
