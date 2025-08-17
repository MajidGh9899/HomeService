package ir.maktab127.service;

import ir.maktab127.entity.Payment;
import ir.maktab127.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository  paymentRepository;

    @Transactional
    @Override
    public Payment save(Payment payment) {
return paymentRepository.save(payment);
    }

    @Override
    public Payment findById(Long id) {
        return null;
    }

    @Override
    public Payment findByToken(String token) {
        return null;
    }
    @Transactional

    @Override
    public void delete(Long id) {

    }

    @Override
    public Long getUserByToken(String token) {
        return 0L;
    }

    @Override
    public Payment findByUserId(Long userId) {
        return null;
    }
}
