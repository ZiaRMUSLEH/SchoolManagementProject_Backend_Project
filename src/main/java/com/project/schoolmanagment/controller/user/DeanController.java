package com.project.schoolmanagment.controller.user;

import com.project.schoolmanagment.payload.request.user.DeanRequest;
import com.project.schoolmanagment.payload.response.message.ResponseMessage;
import com.project.schoolmanagment.payload.response.user.DeanResponse;
import com.project.schoolmanagment.service.user.DeanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dean")
@PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
public class DeanController {

    private final DeanService deanService;

    @PostMapping("/save")
    public ResponseMessage<DeanResponse>saveDean(@RequestBody @Valid DeanRequest deanRequest){
        return deanService.saveDean(deanRequest);
    }

    @PutMapping("/update/{userId}")
    public ResponseMessage<DeanResponse>updateDeanById(@PathVariable Long userId, @RequestBody @Valid DeanRequest deanRequest){
        return deanService.updateDeanById(userId,deanRequest);
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseMessage deleteDeanById(@PathVariable Long userId){
        return deanService.deleteDeanById(userId);
    }



    @GetMapping("/getManagerById/{userId}")
    public DeanResponse getDeanById(@PathVariable Long userId){
        return deanService.getDeanById(userId);

    }

    @GetMapping("/getAll")
    public List<DeanResponse> getAllDeans(){
        return deanService.getAllDeans();
    }


    @GetMapping("/getAllDeansByPage")
    public Page<DeanResponse> getAllDeansByPage(
            @RequestParam(value = "page")int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "sort") String sort,
            @RequestParam(defaultValue = "desc",value = "type") String type
    ){
        return deanService.getAllDeansByPage(page,size,sort,type);

    }

    @GetMapping("/getDeanByUsername/{username}")
    public ResponseEntity<DeanResponse> findDeanByUsername(@PathVariable String username){
        return ResponseEntity.ok(deanService.findDeanByUsername(username));
    }



    @GetMapping("/getDeanByName")
    public ResponseEntity<List<DeanResponse>>getDeanByName(@RequestParam String nameOrSurname){
        return ResponseEntity.ok(deanService.getDeanByName(nameOrSurname));
    }


}
