package ir.maktab127.service;

import ir.maktab127.entity.ServiceCategory;
import ir.maktab127.repository.ServiceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ServiceCategoryServiceImpl implements ServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;

    @Transactional
    @Override
    public ServiceCategory save(ServiceCategory serviceCategory) { return serviceCategoryRepository.save(serviceCategory); }
    @Override
    public Optional<ServiceCategory> findById(Long id) { return serviceCategoryRepository.findById(id); }
    @Override
    public Optional<ServiceCategory> findByNameAndParentId(String name, Long parentId) { return serviceCategoryRepository.findByNameAndParentId(name, parentId); }
    @Override
    public Page<ServiceCategory> getAll(Pageable page) { return serviceCategoryRepository.findAll(page); }
    @Override
    public void delete(Long id) { serviceCategoryRepository.findById(id).ifPresent(serviceCategoryRepository::delete); }
}