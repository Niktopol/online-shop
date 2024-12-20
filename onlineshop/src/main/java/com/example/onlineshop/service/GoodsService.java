package com.example.onlineshop.service;

import com.example.goods.GoodsServiceGrpc;
import com.example.onlineshop.model.dto.AlterGoodDTO;
import com.example.onlineshop.model.dto.GoodAddDTO;
import com.example.onlineshop.model.dto.GoodDTO;
import com.example.types.*;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.example.onlineshop.service.OrdersService.getStringResponseStreamObserver;

@Component
public class GoodsService {
    @GrpcClient("goods")
    GoodsServiceGrpc.GoodsServiceStub stub;

    public CompletableFuture<List<GoodDTO>> getGoodsList(){
        CompletableFuture<List<GoodDTO>> future = new CompletableFuture<>();

        stub.getGoodsList(Empty.getDefaultInstance(), new StreamObserver<GoodList>() {
            List<GoodDTO> goods;
            @Override
            public void onNext(GoodList goodsList) {
                goods = goodsList.getGoodsList().stream().map(good -> new GoodDTO(
                        good.getId(),
                        good.getName(),
                        good.getPrice(),
                        good.getAmount(),
                        good.getCanBeSold())).toList();
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onCompleted() {
                future.complete(goods);
            }
        });

        return future;
    }

    public CompletableFuture<GoodDTO> getGood(Long id){
        CompletableFuture<GoodDTO> future = new CompletableFuture<>();
        final GoodDTO[] goodOut = new GoodDTO[1];

        stub.getGood(Id.newBuilder().setId(id).build(), new StreamObserver<Good>() {
            @Override
            public void onNext(Good good) {
                goodOut[0] = new GoodDTO(good.getId(), good.getName(), good.getPrice(), good.getAmount(), good.getCanBeSold());
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onCompleted() {
                future.complete(goodOut[0]);
            }
        });
        return future;
    }

    public CompletableFuture<List<GoodDTO>> findGoods(String name){
        CompletableFuture<List<GoodDTO>> future = new CompletableFuture<>();

        stub.findGoods(Name.newBuilder().setName(name).build(), new StreamObserver<GoodList>() {
            List<GoodDTO> goods;
            @Override
            public void onNext(GoodList goodsList) {
                goods = goodsList.getGoodsList().stream().map(good -> new GoodDTO(
                        good.getId(),
                        good.getName(),
                        good.getPrice(),
                        good.getAmount(),
                        good.getCanBeSold())).toList();
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onCompleted() {
                future.complete(goods);
            }
        });

        return future;
    }

    public CompletableFuture<String> addGoods(List<GoodAddDTO> goods){
        CompletableFuture<String> future = new CompletableFuture<>();

        final StreamObserver<StringResponse> responseObserver = getStringResponseStreamObserver(future);

        stub.addGoods(GoodAddInfoList.newBuilder()
                .addAllInfos(goods.stream().map(good -> GoodAddInfo.newBuilder()
                        .setName(good.getName())
                        .setPrice(good.getPrice()).build()).toList()).build(), responseObserver);

        return future;
    }

    private GoodAlterInfo constructGoodAlterInfo(AlterGoodDTO good){
        GoodAlterInfo.Builder builder = GoodAlterInfo.newBuilder();
        builder.setId(good.getId());
        if (good.getName() != null){
            builder.setName(good.getName());
        }
        if (good.getPrice() != null){
            builder.setPrice(good.getPrice());
        }
        if (good.getAmount() != null){
            builder.setAmount(good.getAmount());
        }
        if (good.getCanBeSold() != null){
            builder.setCanBeSold(good.getCanBeSold());
        }
        return builder.build();
    }

    public CompletableFuture<String> alterGoods(List<AlterGoodDTO> goods){
        CompletableFuture<String> future = new CompletableFuture<>();

        final StreamObserver<StringResponse> responseObserver = getStringResponseStreamObserver(future);

        stub.alterGoods(GoodAlterInfoList.newBuilder()
                .addAllInfos(goods.stream().map(this::constructGoodAlterInfo).toList()).build(), responseObserver);

        return future;
    }
}
