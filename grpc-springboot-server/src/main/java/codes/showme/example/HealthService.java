package codes.showme.example;

import grpc.health.v1.HealthGrpc;
import grpc.health.v1.HealthOuterClass;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class HealthService extends HealthGrpc.HealthImplBase{
    @Override
    public void check(HealthOuterClass.HealthCheckRequest request, StreamObserver<HealthOuterClass.HealthCheckResponse> responseObserver) {

        final HealthOuterClass.HealthCheckResponse.Builder builder = HealthOuterClass.HealthCheckResponse.newBuilder();

        // just for testing
        builder.setStatus(HealthOuterClass.HealthCheckResponse.ServingStatus.SERVING);


        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
