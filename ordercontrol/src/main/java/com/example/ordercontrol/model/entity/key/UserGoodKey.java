package com.example.ordercontrol.model.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
public class UserGoodKey implements Serializable {
    @Column(name = "user_id")
    Long userId;

    @Column(name = "good_id")
    Long goodId;

    @Override
    public int hashCode() {
        return (String.valueOf(userId) + String.valueOf(goodId)).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(((UserGoodKey) obj).getUserId(), this.userId) &&
                Objects.equals(((UserGoodKey) obj).getGoodId(), this.goodId);
    }
}
