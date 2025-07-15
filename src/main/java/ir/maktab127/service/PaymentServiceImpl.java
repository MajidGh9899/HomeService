package ir.maktab127.service;

import ir.maktab127.entity.Payment;
import ir.maktab127.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository  paymentRepository;

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
