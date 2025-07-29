package ir.maktab127.service;

import ir.maktab127.dto.ServiceHistoryDetailDto;
import ir.maktab127.dto.ServiceHistoryFilterDto;
import ir.maktab127.dto.ServiceHistorySummaryDto;
import ir.maktab127.entity.ServiceCategory;

import java.util.List;
import java.util.Optional;

public interface ServiceCategoryService {
    ServiceCategory save(ServiceCategory serviceCategory);
    Optional<ServiceCategory> findById(Long id);
    Optional<ServiceCategory> findByNameAndParentId(String name, Long parentId);//TODO: add parentId to ServiceCategory
    List<ServiceCategory> getAll();
    void delete(Long id);
}