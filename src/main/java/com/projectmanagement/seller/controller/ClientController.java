package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.ClientDtos.ClientRequestDto;
import com.projectmanagement.seller.dto.ClientDtos.ClientResponseDto;
import com.projectmanagement.seller.dto.ProjectDtos.ProjectResponseDto;
import com.projectmanagement.seller.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellerservice/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/createClient")
    public ResponseEntity<StandardResponse<ClientResponseDto>> createClient(@Valid @RequestBody ClientRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(requestDto));
    }

    @GetMapping("/getAllClients")
    public ResponseEntity<StandardResponse<Page<ClientResponseDto>>> getAllClients(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(clientService.getClients(search, status, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/getClientById/{id}")
    public ResponseEntity<StandardResponse<ClientResponseDto>> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PutMapping("/updateClient/{id}")
    public ResponseEntity<StandardResponse<ClientResponseDto>> updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequestDto requestDto) {
        return ResponseEntity.ok(clientService.updateClient(id, requestDto));
    }

    @GetMapping("/getClientProjects/{id}")
    public ResponseEntity<StandardResponse<List<ProjectResponseDto>>> getClientProjects(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientProjects(id));
    }
}
