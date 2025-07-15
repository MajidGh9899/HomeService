package ir.maktab127.service;

import ir.maktab127.entity.Payment;

public interface PaymentService {

    Payment save(Payment payment);
    Payment findById(Long id);
    Payment findByToken(String token);
    void delete(Long id);
    Long getUserByToken(String token);
    Payment findByUserId(Long userId);


}
