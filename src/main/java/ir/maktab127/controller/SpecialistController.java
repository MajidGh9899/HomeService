package ir.maktab127.controller;

import ir.maktab127.dto.*;
import ir.maktab127.entity.Proposal;
import ir.maktab127.entity.user.Specialist;
import ir.maktab127.service.ProposalService;
import ir.maktab127.service.SpecialistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/specialist")
@Validated
public class SpecialistController {
    private final SpecialistService specialistService;
    private final ProposalService proposalService;

    @Autowired
    public SpecialistController(SpecialistService specialistService, ProposalService proposalService) {
        this.specialistService = specialistService;
        this.proposalService = proposalService;
    }

    @PostMapping("/register")
    public ResponseEntity<SpecialistResponseDto> register(@Valid @RequestBody SpecialistRegisterDto dto) {
        Specialist specialist = SpecialistMapper.toEntity(dto);
        Specialist saved = specialistService.register(specialist);
        return ResponseEntity.ok(SpecialistMapper.toResponseDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialistResponseDto> getById(@PathVariable Long id) {
        Optional<Specialist> specialist = specialistService.findById(id);
        return specialist.map(s -> ResponseEntity.ok(SpecialistMapper.toResponseDto(s)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<SpecialistResponseDto> getAll() {
        return specialistService.getAll().stream()
                .map(SpecialistMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        specialistService.delete(id);
        return ResponseEntity.noContent().build();
    }
    //
    @PostMapping("/login")
    public ResponseEntity<SpecialistResponseDto> login(@Valid @RequestBody SpecialistLoginDto dto) {
        Optional<Specialist> specialist = specialistService.login(dto.getEmail(), dto.getPassword());
        return specialist
                .map(SpecialistMapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401).build());
    }
    //
    @PutMapping("/{id}/update-info")
    public ResponseEntity<Void> updateInfo(@PathVariable Long id, @Valid @RequestBody SpecialistUpdateDto dto) {
        try {
            specialistService.updateInfo(id, dto);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(null); // Conflict
        }
    }
    //offer to do a service
    @PostMapping("/addProposal")
    public ResponseEntity<ProposalResponseDto> registerProposal(@Valid @RequestBody ProposalRegisterDto dto) {
        try {
            Proposal proposal = proposalService.registerProposal(dto);
            return ResponseEntity.ok(ProposalMapper.toResponseDto(proposal));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(409).build();
        }
    }
    //  completed

}
