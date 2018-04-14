package codes.showme.example;

import codes.showme.examples.GreeterGrpc;
import codes.showme.examples.GreeterOuterClass;
import io.grpc.stub.StreamObserver;
import io.prometheus.client.Histogram;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GRpcService()
public class GreeterService extends GreeterGrpc.GreeterImplBase {
    private static Logger log = LoggerFactory.getLogger(GreeterService.class);


    static final Histogram requestLatency = Histogram.build()
            .name("status_request_duration_seconds")
            .help("Status Receiver process duration (seconds).")
            .register();    // Register must be called to add it to the output


    @Override
    public void sayHello(GreeterOuterClass.HelloRequest request, StreamObserver<GreeterOuterClass.HelloReply> responseObserver) {
        Histogram.Timer requestTimer = requestLatency.startTimer();
        try {
            String message = "Hello " + request.getName();
            log.info("req: {}", request.getName());
            final GreeterOuterClass.HelloReply.Builder replyBuilder = GreeterOuterClass.HelloReply.newBuilder().setMessage(message);
            responseObserver.onNext(replyBuilder.build());
            responseObserver.onCompleted();

            log.info("Returning " + message);
        } finally {
            requestTimer.observeDuration();
        }
    }
}


