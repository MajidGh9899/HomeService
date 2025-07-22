package ir.maktab127.service;

import ir.maktab127.dto.OrderRegisterDto;
import ir.maktab127.dto.payment.PaymentRequestDto;
import ir.maktab127.entity.*;
import ir.maktab127.entity.user.AccountStatus;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.exception.OrderException;
import ir.maktab127.exception.WalletException;
import ir.maktab127.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {



}