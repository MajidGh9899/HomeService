package ir.maktab127.service;

import ir.maktab127.dto.CommentRegisterDto;
import ir.maktab127.entity.Comment;
import ir.maktab127.entity.Order;
import ir.maktab127.entity.OrderStatus;
import ir.maktab127.entity.user.Customer;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.repository.CommentRepository;
import ir.maktab127.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerService customerRepository;

    @Mock
    private SpecialistService specialistRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment comment;
    private Order order;
    private Customer customer;
    private Specialist specialist;
    private CommentRegisterDto commentRegisterDto;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);

        specialist = new Specialist();
        specialist.setId(2L);

        order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setSpecialist(specialist);
        order.setStatus(OrderStatus.COMPLETED);

        comment = new Comment();
        comment.setId(1L);
        comment.setCustomer(customer);
        comment.setSpecialist(specialist);
        comment.setRating(5);
        comment.setText("Great service");
        comment.setCreatedAt(LocalDateTime.now());
        comment.setOrder(order);

        commentRegisterDto = new CommentRegisterDto();
        commentRegisterDto.setCustomerId(1L);
        commentRegisterDto.setSpecialistId(2L);
        commentRegisterDto.setRating(5);
        commentRegisterDto.setText("Great service");
    }

    @Test
    void save_ValidComment_ReturnsSavedComment() {
        when(commentRepository.save(comment)).thenReturn(comment);

        Comment result = commentService.save(comment);

        assertNotNull(result);
        assertEquals(comment, result);
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void findById_Exists_ReturnsComment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        Optional<Comment> result = commentService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(comment, result.get());
    }

    @Test
    void findById_NotExists_ReturnsEmpty() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Comment> result = commentService.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void getAll_ReturnsAllComments() {
        List<Comment> comments = Arrays.asList(comment);
        when(commentRepository.findAll()).thenReturn(comments);

        List<Comment> result = commentService.getAll();

        assertEquals(1, result.size());
        assertEquals(comment, result.get(0));
    }

    @Test
    void delete_CommentExists_DeletesSuccessfully() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.delete(1L);

        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void delete_CommentNotExists_NoAction() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        commentService.delete(1L);

        verify(commentRepository, never()).delete(any());
    }



    @Test
    void registerComment_OrderNotFound_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commentService.registerComment(commentRegisterDto, 1L));

        assertEquals("Order not found", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void registerComment_OrderNotCompleted_ThrowsException() {
        order.setStatus(OrderStatus.IN_PROGRESS);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> commentService.registerComment(commentRegisterDto, 1L));

        assertEquals("Order is not completed yet", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void registerComment_InvalidCustomer_ThrowsException() {
        commentRegisterDto.setCustomerId(3L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commentService.registerComment(commentRegisterDto, 1L));

        assertEquals("Invalid customer or specialist for this order", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }



    @Test
    void getAverageRatingForSpecialist_CommentsExist_ReturnsAverage() {
        Comment secondComment = new Comment();
        secondComment.setId(2L);
        secondComment.setRating(3);
        secondComment.setSpecialist(specialist);
        secondComment.setCustomer(customer);
        secondComment.setOrder(order);
        secondComment.setText("Good service");
        secondComment.setCreatedAt(LocalDateTime.now());
        List<Comment> comments = Arrays.asList(comment, secondComment);
        when(commentRepository.findBySpecialistId(2L)).thenReturn(comments);

        Double result = commentService.getAverageRatingForSpecialist(2L);

        assertEquals(4.0, result);
    }

    @Test
    void getAverageRatingForSpecialist_NoComments_ReturnsNull() {
        when(commentRepository.findBySpecialistId(2L)).thenReturn(Collections.emptyList());

        Double result = commentService.getAverageRatingForSpecialist(2L);

        assertNull(result);
    }

    @Test
    void getOrderRatingForSpecialist_CommentExists_ReturnsRating() {
        List<Comment> comments = Arrays.asList(comment);
        when(commentRepository.findBySpecialistIdAndOrderId(2L, 1L)).thenReturn(comments);

        Integer result = commentService.getOrderRatingForSpecialist(2L, 1L);

        assertEquals(5, result);
    }

    @Test
    void getOrderRatingForSpecialist_NoComment_ReturnsNull() {
        when(commentRepository.findBySpecialistIdAndOrderId(2L, 1L)).thenReturn(Collections.emptyList());

        Integer result = commentService.getOrderRatingForSpecialist(2L, 1L);

        assertNull(result);
    }
}