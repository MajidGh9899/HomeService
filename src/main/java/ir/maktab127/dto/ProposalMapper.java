package ir.maktab127.dto;

import ir.maktab127.entity.Proposal;

import java.time.format.DateTimeFormatter;

public class ProposalMapper {
    public static ProposalResponseDto toResponseDto(Proposal proposal) {
        ProposalResponseDto dto = new ProposalResponseDto();
        dto.setId(proposal.getId());
        dto.setSpecialistName(proposal.getSpecialist() != null ? proposal.getSpecialist().getFirstName() + " " + proposal.getSpecialist().getLastName() : null);
        dto.setOrderId(proposal.getOrder() != null ? proposal.getOrder().getId() : null);
        dto.setProposedPrice(proposal.getProposedPrice());
        dto.setProposedStartTime(proposal.getProposedStartTime() != null ? proposal.getProposedStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        dto.setDurationInHours(proposal.getDurationInHours());
        dto.setCreatedAt(proposal.getCreatedAt() != null ? proposal.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        return dto;
    }
}
