package ir.maktab127.service;

import ir.maktab127.dto.SpecialistUpdateDto;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.OrderRepository;
import ir.maktab127.repository.SpecialistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Service
public class SpecialistServiceImpl implements SpecialistService {
    private final SpecialistRepository specialistRepository;
    private final OrderRepository orderRepository;

@Autowired
    public SpecialistServiceImpl(SpecialistRepository specialistRepository, OrderRepository orderRepository) {
        this.specialistRepository = specialistRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public Specialist register(Specialist specialist) {
        specialist.setStatus(AccountStatus.NEW);
        specialist.setRegisterDate(LocalDateTime.now());
        return specialistRepository.save(specialist);
    }

    @Override
    public Optional<Specialist> findById(Long id) {
        return specialistRepository.findById(id);
    }

    @Override
    public Optional<Specialist> findByEmail(String email) {
        return specialistRepository.findByEmail(email);
    }

    @Override
    public List<Specialist> getAll() {
        return specialistRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        specialistRepository.findById(id).ifPresent(specialistRepository::delete);
    }
    @Override
    public Optional<Specialist> login(String email, String password) {
        return specialistRepository.findByEmail(email)
                .filter(s -> s.getPassword().equals(password) && s.getStatus() == AccountStatus.APPROVED);
    }
    @Override
    public void updateInfo(Long specialistId, SpecialistUpdateDto dto) {
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new IllegalArgumentException("Specialist not found"));

        // بررسی نداشتن کار فعال
        boolean hasActiveOrder = orderRepository.findAll().stream()
                .anyMatch(order -> order.getSpecialist() != null
                        && order.getSpecialist().getId().equals(specialistId)
                        && order.getStatus() == OrderStatus.IN_PROGRESS);

        if (hasActiveOrder) {
            throw new IllegalStateException("Specialist has active work and cannot update info now.");
        }

        specialist.setEmail(dto.getEmail());
        specialist.setPassword(dto.getPassword());
        specialist.setProfileImagePath(dto.getProfileImagePath());
        specialist.setStatus(AccountStatus.PENDING);
        specialistRepository.save(specialist);
    }
}