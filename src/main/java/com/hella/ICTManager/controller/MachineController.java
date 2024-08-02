package com.hella.ICTManager.controller;

import com.hella.ICTManager.entity.Machine;
import com.hella.ICTManager.service.impl.MachineServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/machines")
public class MachineController {
    private final MachineServiceImpl machineService;

    public MachineController(MachineServiceImpl machineService) {
        this.machineService = machineService;
    }

    @PostMapping
    public void save(@RequestBody Machine machine) {
        machineService.save(machine);
    }

    @GetMapping("/{id}")
    public Machine findById(@PathVariable long id) {
        return machineService.findById(id);
    }

    @GetMapping
    public List<Machine> getMachines() {
        return machineService.findAll();
    }

    @PutMapping("/{id}")
    public void update(@PathVariable long id, @RequestBody Machine machine) {
        machineService.update(id, machine);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable long id) {
        machineService.deleteById(id);
    }
}
