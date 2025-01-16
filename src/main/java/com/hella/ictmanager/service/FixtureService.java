package com.hella.ictmanager.service;

import com.hella.ictmanager.entity.Fixture;
import com.hella.ictmanager.model.FixtureDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FixtureService {
    void save(FixtureDTO fixtureDTO);

    FixtureDTO findById(long id);

    List<FixtureDTO> findAll();

    Fixture findEntityById(long id);

    void update(long id, FixtureDTO fixtureDTO);

    void deleteById(long id);

    void addFixtureToMachine(long fixtureId, long machineId);

    void createMaintenanceFixtureReport();

    void removeFixtureFromMachine(long fixtureId);
}
