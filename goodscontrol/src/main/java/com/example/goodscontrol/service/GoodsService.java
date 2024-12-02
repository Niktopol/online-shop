package com.example.goodscontrol.service;

import com.example.goods.GoodsServiceGrpc;
import com.example.goodscontrol.repository.GoodsRepository;
import com.example.types.*;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@GrpcService
public class GoodsService extends GoodsServiceGrpc.GoodsServiceImplBase {
    GoodsRepository repository;

    @Override
    public void getGoodsList(Empty request, StreamObserver<com.example.types.GoodList> responseObserver) {
        List<com.example.goodscontrol.model.entity.Good> goods = repository.findAll();
        responseObserver.onNext(GoodList.newBuilder()
                .addAllGoods(goods.stream().map(good -> Good.newBuilder()
                        .setId(good.getId())
                        .setName(good.getName())
                        .setPrice(good.getPrice())
                        .setAmount(good.getAmount())
                        .setCanBeSold(good.getCanBeSold()).build()).toList()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getGood(Id request, StreamObserver<Good> responseObserver) {
        Optional<com.example.goodscontrol.model.entity.Good> goodOpt = repository.findById(request.getId());
        if (goodOpt.isPresent()){
            com.example.goodscontrol.model.entity.Good good = goodOpt.get();
            responseObserver.onNext(Good.newBuilder()
                    .setId(good.getId())
                    .setName(good.getName())
                    .setPrice(good.getPrice())
                    .setAmount(good.getAmount())
                    .setCanBeSold(good.getCanBeSold()).build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Good not found").asRuntimeException());
        }
    }

    @Override
    public void findGoods(Name request, StreamObserver<com.example.types.GoodList> responseObserver) {
        List<com.example.goodscontrol.model.entity.Good> goods = repository.findAllByNameContainingIgnoreCase(request.getName());
        responseObserver.onNext(GoodList.newBuilder()
                .addAllGoods(goods.stream().map(good -> Good.newBuilder()
                        .setId(good.getId())
                        .setName(good.getName())
                        .setPrice(good.getPrice())
                        .setAmount(good.getAmount())
                        .setCanBeSold(good.getCanBeSold()).build()).toList()).build());
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void addGoods(GoodAddInfoList request, StreamObserver<StringResponse> responseObserver) {
        List<com.example.goodscontrol.model.entity.Good> goods = request.getInfosList().stream().map(good ->
                new com.example.goodscontrol.model.entity.Good(good.getName(), good.getPrice())).toList();
        try {
            repository.saveAllAndFlush(goods);
        } catch (DataIntegrityViolationException e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(
                    "List contains good with a name that already exists").asRuntimeException());
            return;
        } catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
            return;
        }
        responseObserver.onNext(StringResponse.newBuilder()
                .setResponse("Goods added successfully").build());
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void alterGoods(GoodAlterInfoList request, StreamObserver<StringResponse> responseObserver) {
        List<com.example.goodscontrol.model.entity.Good> goods = new ArrayList<>();
        for (GoodAlterInfo info: request.getInfosList()){
            Optional<com.example.goodscontrol.model.entity.Good> goodOpt = repository.findById(info.getId());
            if (goodOpt.isPresent()){
                if (info.hasName()) {
                    goodOpt.get().setName(info.getName());
                }
                if (info.hasPrice()) {
                    goodOpt.get().setPrice(info.getPrice());
                }
                if (info.hasAmount()) {
                    goodOpt.get().setAmount(info.getAmount());
                }
                if (info.hasCanBeSold()) {
                    goodOpt.get().setCanBeSold(info.getCanBeSold());
                }
                goods.add(goodOpt.get());
            } else {
                responseObserver.onError(Status.NOT_FOUND.withDescription(
                        "List contains non-existing goods").asRuntimeException());
                return;
            }
        }
        try{
            repository.saveAllAndFlush(goods);
        } catch (OptimisticLockException e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            responseObserver.onError(Status.INTERNAL.withDescription(
                    "Listed good has been already modified").asRuntimeException());
            return;
        } catch (DataIntegrityViolationException e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(
                    "List contains good with a name tha already exists").asRuntimeException());
            return;
        } catch (Exception e){
            responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
            return;
        }
        responseObserver.onNext(StringResponse.newBuilder()
                .setResponse("Goods modified successfully").build());
        responseObserver.onCompleted();
    }
}
