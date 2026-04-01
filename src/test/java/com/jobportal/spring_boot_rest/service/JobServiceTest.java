package com.jobportal.spring_boot_rest.service;

import com.jobportal.spring_boot_rest.exception.ResourceNotFoundException;
import com.jobportal.spring_boot_rest.model.JobPost;
import com.jobportal.spring_boot_rest.repo.JobRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepo repo;

    @InjectMocks
    private JobService service;

    @Test
    void getJobOrThrow_ShouldReturnJob_WhenJobExists() {
        JobPost expected = new JobPost(1, "Java Developer", "desc", 2, List.of("Java"));
        when(repo.findById(1)).thenReturn(Optional.of(expected));

        JobPost result = service.getJobOrThrow(1);

        assertEquals(expected, result);
        verify(repo).findById(1);
    }

    @Test
    void getJobOrThrow_ShouldThrow_WhenJobMissing() {
        when(repo.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getJobOrThrow(99));
        verify(repo).findById(99);
    }

    @Test
    void updateJob_ShouldThrow_WhenJobMissing() {
        JobPost request = new JobPost(7, "profile", "desc", 1, List.of("A"));
        when(repo.existsById(7)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.updateJob(request));
        verify(repo, never()).save(any(JobPost.class));
    }

    @Test
    void deleteJob_ShouldThrow_WhenJobMissing() {
        when(repo.existsById(11)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteJob(11));
        verify(repo, never()).deleteById(anyInt());
    }

    @Test
    void loadIfNeeded_ShouldNotSave_WhenSeedAlreadyExists() {
        when(repo.existsById(anyInt())).thenReturn(true);

        boolean created = service.loadIfNeeded();

        assertFalse(created);
        verify(repo, never()).saveAll(anyList());
    }

    @Test
    void loadIfNeeded_ShouldSave_WhenAnySeedMissing() {
        when(repo.existsById(1)).thenReturn(true);
        when(repo.existsById(2)).thenReturn(false);
        when(repo.existsById(3)).thenReturn(true);
        when(repo.existsById(4)).thenReturn(true);
        when(repo.existsById(5)).thenReturn(true);

        boolean created = service.loadIfNeeded();

        assertTrue(created);
        verify(repo).saveAll(anyList());
    }
}
