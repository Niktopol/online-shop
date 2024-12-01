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
    public void getGoodsList(Empty request, StreamObserver<Good> responseObserver) {
        List<com.example.goodscontrol.model.entity.Good> goods = repository.findAll();
        for (com.example.goodscontrol.model.entity.Good good: goods){
            responseObserver.onNext(Good.newBuilder()
                    .setId(good.getId())
                    .setName(good.getName())
                    .setPrice(good.getPrice())
                    .setAmount(good.getAmount())
                    .setCanBeSold(good.getCanBeSold()).build());
        }
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
    public void findGoods(Name request, StreamObserver<Good> responseObserver) {
        List<com.example.goodscontrol.model.entity.Good> goods = repository.findAllByNameContainingIgnoreCase(request.getName());
        for (com.example.goodscontrol.model.entity.Good good: goods){
            responseObserver.onNext(Good.newBuilder()
                    .setId(good.getId())
                    .setName(good.getName())
                    .setPrice(good.getPrice())
                    .setAmount(good.getAmount())
                    .setCanBeSold(good.getCanBeSold()).build());
        }
        responseObserver.onCompleted();
    }

    @Transactional
    @Override
    public void addGood(GoodAddInfo request, StreamObserver<StringResponse> responseObserver) {
        com.example.goodscontrol.model.entity.Good good = new com.example.goodscontrol.model.entity.Good(
                request.getName(),
                request.getPrice()
        );
        try {
            repository.saveAndFlush(good);
        } catch (DataIntegrityViolationException e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(
                    String.format("Good with name %s already exists", request.getName())).asRuntimeException());
            return;
        } catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
            return;
        }
        responseObserver.onNext(StringResponse.newBuilder()
                .setResponse("Good added successfully")
                .setResponseCode(100).build());
        responseObserver.onCompleted();
    }

    @Transactional
    private void saveGoods(List<com.example.goodscontrol.model.entity.Good> goods){
        repository.saveAll(goods);
        repository.flush();
    }

    @Override
    public StreamObserver<GoodAddInfo> addGoods(StreamObserver<StringResponse> responseObserver) {
        return new StreamObserver<GoodAddInfo>() {
            final List<com.example.goodscontrol.model.entity.Good> goods = new ArrayList<>();

            @Override
            public void onNext(GoodAddInfo goodAddInfo) {
                goods.add(new com.example.goodscontrol.model.entity.Good(
                        goodAddInfo.getName(),
                        goodAddInfo.getPrice()));
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onCompleted() {
                try {
                    saveGoods(goods);
                } catch (DataIntegrityViolationException e){
                    responseObserver.onError(Status.ALREADY_EXISTS.withDescription(
                            "List contains good with a name that already exists").asRuntimeException());
                    return;
                } catch (Exception e){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
                    return;
                }
                responseObserver.onNext(StringResponse.newBuilder()
                        .setResponse("Goods added successfully")
                        .setResponseCode(100).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Transactional
    @Override
    public void alterGood(GoodAlterInfo request, StreamObserver<StringResponse> responseObserver) {
        Optional<com.example.goodscontrol.model.entity.Good> goodOpt = repository.findById(request.getId());
        if (goodOpt.isPresent()){
            com.example.goodscontrol.model.entity.Good good = goodOpt.get();
            if (request.hasName()){
                good.setName(request.getName());
            }
            if (request.hasPrice()){
                good.setPrice(request.getPrice());
            }
            if (request.hasAmount()){
                good.setAmount(request.getAmount());
            }
            if (request.hasCanBeSold()){
                good.setCanBeSold(request.getCanBeSold());
            }
            try{
                repository.saveAndFlush(good);
            } catch (OptimisticLockException e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                responseObserver.onError(Status.INTERNAL.withDescription(
                        "Listed good has been already modified").asRuntimeException());
                return;
            } catch (DataIntegrityViolationException e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                responseObserver.onError(Status.ALREADY_EXISTS.withDescription(
                        String.format("Good with a name '%s' already exists", good.getName())).asRuntimeException());
                return;
            } catch (Exception e){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
                return;
            }
        }else {
            responseObserver.onError(Status.NOT_FOUND.withDescription(
                    String.format("Good with id %d does not exist", request.getId())).asRuntimeException());
            return;
        }
        responseObserver.onNext(StringResponse.newBuilder()
                .setResponse("Good modified successfully")
                .setResponseCode(100).build());
        responseObserver.onCompleted();
    }

    @Transactional
    private void saveGoodChanges(List<com.example.goodscontrol.model.entity.Good> goods){
        repository.saveAll(goods);
        repository.flush();
    }

    @Override
    public io.grpc.stub.StreamObserver<GoodAlterInfo> alterGoods(StreamObserver<StringResponse> responseObserver) {
        return new StreamObserver<GoodAlterInfo>() {
            final List<GoodAlterInfo> goodsInfo = new ArrayList<>();
            @Override
            public void onNext(GoodAlterInfo goodAlterInfo) {
                goodsInfo.add(goodAlterInfo);
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onCompleted() {
                List<com.example.goodscontrol.model.entity.Good> goods = new ArrayList<>();
                for (GoodAlterInfo goodInfo: goodsInfo){
                    Optional<com.example.goodscontrol.model.entity.Good> goodOpt = repository.findById(goodInfo.getId());
                    if (goodOpt.isPresent()) {
                        com.example.goodscontrol.model.entity.Good good = goodOpt.get();
                        if (goodInfo.hasName()) {
                            good.setName(goodInfo.getName());
                        }
                        if (goodInfo.hasPrice()) {
                            good.setPrice(goodInfo.getPrice());
                        }
                        if (goodInfo.hasAmount()) {
                            good.setAmount(goodInfo.getAmount());
                        }
                        if (goodInfo.hasCanBeSold()) {
                            good.setCanBeSold(goodInfo.getCanBeSold());
                        }
                        goods.add(good);
                    }else {
                        responseObserver.onError(Status.NOT_FOUND.withDescription(
                                "List contains non-existing goods").asRuntimeException());
                        return;
                    }
                }
                try{
                    saveGoodChanges(goods);
                } catch (OptimisticLockException e){
                    responseObserver.onError(Status.INTERNAL.withDescription(
                            "Listed good has been already modified").asRuntimeException());
                    return;
                } catch (DataIntegrityViolationException e){
                    responseObserver.onError(Status.ALREADY_EXISTS.withDescription(
                            "List contains good with a name tha already exists").asRuntimeException());
                    return;
                } catch (Exception e){
                    responseObserver.onError(Status.INTERNAL.withDescription("Unknown error").asRuntimeException());
                    return;
                }
                responseObserver.onNext(StringResponse.newBuilder()
                        .setResponse("Goods modified successfully")
                        .setResponseCode(100).build());
                responseObserver.onCompleted();
            }
        };
    }
}
