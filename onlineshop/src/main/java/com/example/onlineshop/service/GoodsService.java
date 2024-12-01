package com.example.onlineshop.service;

import com.example.goods.GoodsServiceGrpc;
import com.example.onlineshop.model.dto.AlterGoodDTO;
import com.example.onlineshop.model.dto.GoodAddDTO;
import com.example.onlineshop.model.dto.GoodDTO;
import com.example.onlineshop.model.dto.OperationResultDTO;
import com.example.types.*;
import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class GoodsService {
    @GrpcClient("goods")
    GoodsServiceGrpc.GoodsServiceBlockingStub bstub;
    @GrpcClient("goods")
    GoodsServiceGrpc.GoodsServiceStub astub;

    public List<GoodDTO> getGoodsList(){
        try{
            Iterator<Good> goods = bstub.getGoodsList(Empty.getDefaultInstance());
            List<GoodDTO> goodsInfo = new ArrayList<>();

            while (goods.hasNext()){
                Good good = goods.next();
                goodsInfo.add(new GoodDTO(good.getId(), good.getName(), good.getPrice(), good.getAmount(), good.getCanBeSold()));
            }

            return goodsInfo;
        } catch (StatusRuntimeException e) {
            throw e;
        } catch (Exception e){
            throw new RuntimeException("Unexpected error");
        }

        /*CompletableFuture<List<GoodDTO>> future = new CompletableFuture<>();
        List<GoodDTO> goods = new ArrayList<>();

        stub.getGoodsList(Empty.getDefaultInstance(), new StreamObserver<Good>() {
            @Override
            public void onNext(Good good) {
                goods.add(new GoodDTO(good.getId(), good.getName(), good.getPrice(), good.getAmount(), good.getCanBeSold()));
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

        return future;*/
    }

    public GoodDTO getGood(Long id){
        try{
            Good good = bstub.getGood(Id.newBuilder().setId(id).build());
            return new GoodDTO(good.getId(), good.getName(), good.getPrice(), good.getAmount(), good.getCanBeSold());
        } catch (StatusRuntimeException e) {
            throw e;
        } catch (Exception e){
            throw new RuntimeException("Unexpected error");
        }
        /*CompletableFuture<GoodDTO> future = new CompletableFuture<>();
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
        return future;*/
    }

    public List<GoodDTO> findGoods(String name){
        try{
            Iterator<Good> goods = bstub.findGoods(Name.newBuilder().setName(name).build());
            List<GoodDTO> goodsInfo = new ArrayList<>();

            while (goods.hasNext()){
                Good good = goods.next();
                goodsInfo.add(new GoodDTO(good.getId(), good.getName(), good.getPrice(), good.getAmount(), good.getCanBeSold()));
            }

            return goodsInfo;
        } catch (StatusRuntimeException e) {
            throw e;
        } catch (Exception e){
            throw new RuntimeException("Unexpected error");
        }
        /*CompletableFuture<List<GoodDTO>> future = new CompletableFuture<>();
        List<GoodDTO> goods = new ArrayList<>();

        stub.findGoods(Name.newBuilder().setName(name).build(), new StreamObserver<Good>() {
            @Override
            public void onNext(Good good) {
                goods.add(new GoodDTO(good.getId(), good.getName(), good.getPrice(), good.getAmount(), good.getCanBeSold()));
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

        return future;*/
    }

    public CompletableFuture<OperationResultDTO> addGoods(List<GoodAddDTO> goods){
        CompletableFuture<OperationResultDTO> future = new CompletableFuture<>();
        final OperationResultDTO[] result = new OperationResultDTO[1];

        final StreamObserver<StringResponse> responseObserver = new StreamObserver<StringResponse>() {
            @Override
            public void onNext(StringResponse stringResponse) {
                result[0] = new OperationResultDTO(stringResponse.getResponseCode(), stringResponse.getResponse());
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onCompleted() {
                future.complete(result[0]);
            }
        };

        final StreamObserver<GoodAddInfo> request = astub.addGoods(responseObserver);
        goods.stream()
                .map((dto) -> GoodAddInfo.newBuilder().setName(dto.getName()).setPrice(dto.getPrice()).build())
                .forEach(request::onNext);
        request.onCompleted();

        return future;
    }

    public CompletableFuture<OperationResultDTO> addGood(GoodAddDTO good){
        CompletableFuture<OperationResultDTO> future = new CompletableFuture<>();
        final OperationResultDTO[] result = new OperationResultDTO[1];

        final StreamObserver<StringResponse> responseObserver = new StreamObserver<StringResponse>() {
            @Override
            public void onNext(StringResponse stringResponse) {
                result[0] = new OperationResultDTO(stringResponse.getResponseCode(), stringResponse.getResponse());
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onCompleted() {
                future.complete(result[0]);
            }
        };

        astub.addGood(GoodAddInfo.newBuilder().setName(good.getName()).setPrice(good.getPrice()).build(), responseObserver);
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

    public CompletableFuture<OperationResultDTO> alterGoods(List<AlterGoodDTO> goods){
        CompletableFuture<OperationResultDTO> future = new CompletableFuture<>();
        final OperationResultDTO[] result = new OperationResultDTO[1];

        final StreamObserver<StringResponse> responseObserver = new StreamObserver<StringResponse>() {
            @Override
            public void onNext(StringResponse stringResponse) {
                result[0] = new OperationResultDTO(stringResponse.getResponseCode(), stringResponse.getResponse());
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onCompleted() {
                future.complete(result[0]);
            }
        };

        final StreamObserver<GoodAlterInfo> request = astub.alterGoods(responseObserver);
        goods.stream()
                .map(this::constructGoodAlterInfo)
                .forEach(request::onNext);
        request.onCompleted();

        return future;
    }

    public CompletableFuture<OperationResultDTO> alterGood(AlterGoodDTO good){
        CompletableFuture<OperationResultDTO> future = new CompletableFuture<>();
        final OperationResultDTO[] result = new OperationResultDTO[1];

        final StreamObserver<StringResponse> responseObserver = new StreamObserver<StringResponse>() {
            @Override
            public void onNext(StringResponse stringResponse) {
                result[0] = new OperationResultDTO(stringResponse.getResponseCode(), stringResponse.getResponse());
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onCompleted() {
                future.complete(result[0]);
            }
        };

        astub.alterGood(constructGoodAlterInfo(good), responseObserver);
        return future;
    }
}
