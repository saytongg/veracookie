package com.saytongg.veracookie.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.Getter;
import lombok.Setter;

@RedisHash(value = "CookieBannerRecord", timeToLive = 259200L)
@Setter
@Getter
public final class CookieBannerRecord implements Serializable{
    @Id
    @Indexed
    private String link;
    private String image;
    private String textRating;
    private String imageRating;
}
