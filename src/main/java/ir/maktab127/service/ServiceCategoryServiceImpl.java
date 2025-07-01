package ir.maktab127.service;

import ir.maktab127.entity.ServiceCategory;
import ir.maktab127.repository.ServiceCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class ServiceCategoryServiceImpl implements ServiceCategoryService {
    private final ServiceCategoryRepository serviceCategoryRepository;
    @Autowired
    public ServiceCategoryServiceImpl(ServiceCategoryRepository serviceCategoryRepository) {
        this.serviceCategoryRepository = serviceCategoryRepository;
    }
    @Override
    public ServiceCategory save(ServiceCategory serviceCategory) { return serviceCategoryRepository.save(serviceCategory); }
    @Override
    public Optional<ServiceCategory> findById(Long id) { return serviceCategoryRepository.findById(id); }
    @Override
    public Optional<ServiceCategory> findByNameAndParentId(String name, Long parentId) { return serviceCategoryRepository.findByNameAndParentId(name, parentId); }
    @Override
    public List<ServiceCategory> getAll() { return serviceCategoryRepository.findAll(); }
    @Override
    public void delete(Long id) { serviceCategoryRepository.findById(id).ifPresent(serviceCategoryRepository::delete); }
}