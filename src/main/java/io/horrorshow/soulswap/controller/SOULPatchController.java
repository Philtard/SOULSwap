package io.horrorshow.soulswap.controller;


import io.horrorshow.soulswap.data.SOULSwapRepository;
import io.horrorshow.soulswap.exception.ResourceNotFound;
import io.horrorshow.soulswap.data.SOULPatch;
import io.horrorshow.soulswap.service.SOULPatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class SOULPatchController {

    private final SOULPatchService soulPatchService;
    @Deprecated
    private final SOULSwapRepository repository;

    @Autowired
    public SOULPatchController(SOULPatchService soulPatchService, SOULSwapRepository repository) {
        Assert.notNull(soulPatchService, SOULPatchService.class.getName() + " must not be null.");
        this.soulPatchService = soulPatchService;
        Assert.notNull(repository, SOULSwapRepository.class.getName() + " must not be null.");
        this.repository = repository;
    }

    @RequestMapping("/")
    public String index() {
        return "This is SOULSwap!";
    }

    @GetMapping("/rest/soulpatches")
    public List<SOULPatch> getSOULPatches() {
        return repository.findAll();
    }

    @PostMapping("/resst/soulpatches")
    public SOULPatch createSOULPatch(@Valid @RequestBody SOULPatch soulPatch) {
        return repository.save(soulPatch);
    }

    @PutMapping("/rest/soulpatches/{soulpatchId}")
    public SOULPatch updateSOULPatch(@PathVariable Long soulpatchId,
                                     @Valid @RequestBody SOULPatch soulPatch) {
        return repository.findById(soulpatchId)
                .map(patch -> {
                    patch.setAuthor(soulPatch.getAuthor());
                    patch.setDescription(soulPatch.getDescription());
                    patch.setSoulFileName(soulPatch.getSoulFileName());
                    patch.setSoulFileContent(soulPatch.getSoulFileContent());
                    patch.setSoulpatchFileName(soulPatch.getSoulpatchFileName());
                    patch.setSoulpatchFileContent(soulPatch.getSoulpatchFileContent());
                    patch.setAuthor(soulPatch.getAuthor());
                    patch.setNoServings(soulPatch.getNoServings());
                    return repository.save(patch);
                })
                .orElseThrow(() ->
                        new ResourceNotFound(
                                SOULPatch.class.getName() + " not found in repository, soulpatchId " + soulpatchId));
    }

    @DeleteMapping("/rest/soulpatches/{soulpatchId}")
    public ResponseEntity<?> deleteSOULPatch(@PathVariable Long soulpatchId) {
        return repository.findById(soulpatchId)
                .map(soulPatch -> {
                    repository.delete(soulPatch);
                    return ResponseEntity.ok().build();
                })
                .orElseThrow(() ->
                        new ResourceNotFound(
                                SOULPatch.class.getName() + " not found in repository, soulpatchId " + soulpatchId));
    }
}