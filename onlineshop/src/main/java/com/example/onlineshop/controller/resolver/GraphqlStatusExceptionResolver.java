package com.example.onlineshop.controller.resolver;

import com.example.onlineshop.exception.EmptyListException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class GraphqlStatusExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof StatusRuntimeException) {
            GraphqlErrorBuilder<?> builder = GraphqlErrorBuilder.newError()
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation());
            if (((StatusRuntimeException) ex).getStatus().getCode() == Status.Code.NOT_FOUND){
                builder.errorType(ErrorType.NOT_FOUND).message(((StatusRuntimeException) ex).getStatus().getDescription());
            }else if (((StatusRuntimeException) ex).getStatus().getCode() == Status.Code.UNAVAILABLE){
                builder.errorType(ErrorType.INTERNAL_ERROR).message("Service unavailable");
            }else {
                builder.errorType(ErrorType.INTERNAL_ERROR).message(ex.getMessage());
            }
            return builder.build();
        } else if(ex instanceof EmptyListException){
            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
        } else {
            return null;
        }
    }
}
