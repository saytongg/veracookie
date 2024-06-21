package com.saytongg.veracookie.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.saytongg.veracookie.entity.CookieBannerRecord;

import java.util.List;

@Repository
public interface CookieBannerRecordRepository extends CrudRepository<CookieBannerRecord, String> {
    public List<CookieBannerRecord> findByLink(String link);
}
