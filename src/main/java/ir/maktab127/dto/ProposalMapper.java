package ir.maktab127.dto;

import ir.maktab127.entity.Order;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.ProposalStatus;
import ir.maktab127.entity.user.Specialist;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ProposalMapper {
    public static Proposal toEntity(ProposalRegisterDto dto, Order order, Specialist specialist) {
        Proposal proposal = new Proposal();
        proposal.setOrder(order);
        proposal.setSpecialist(specialist);
        proposal.setProposedPrice(dto.getProposedPrice());
        proposal.setProposedStartTime(dto.getStartDate());
        proposal.setEndDate(dto.getEndDate());
        proposal.setDescription(dto.getDescription());
        proposal.setCreateDate(LocalDateTime.now());
        proposal.setStatus(ProposalStatus.PENDING);
        return proposal;
    }

    public static ProposalResponseDto toResponseDto(Proposal proposal) {
        ProposalResponseDto dto = new ProposalResponseDto();
        dto.setId(proposal.getId());
        dto.setOrderId(proposal.getOrder().getId());
        dto.setSpecialistId(proposal.getSpecialist().getId());
        dto.setSpecialistName(proposal.getSpecialist().getFirstName() + " " + proposal.getSpecialist().getLastName());
        dto.setProposedPrice(proposal.getProposedPrice());
        dto.setStartDate(proposal.getProposedStartTime());
        dto.setEndDate(proposal.getEndDate());
        dto.setDescription(proposal.getDescription());
        dto.setCreatedAt(proposal.getCreateDate());
        dto.setStatus(proposal.getStatus());
        return dto;
    }

}
