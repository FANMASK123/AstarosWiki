package com.example.astaroswiki.service;

import com.example.astaroswiki.entity.Tag;
import com.example.astaroswiki.exception.BusinessException;
import com.example.astaroswiki.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Transactional
    public Tag createTag(String name) {
        if (tagRepository.existsByName(name)) {
            throw new BusinessException("标签已存在");
        }

        Tag tag = new Tag();
        tag.setName(name);
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
            .orElseThrow(() -> new BusinessException("标签不存在"));
        tagRepository.delete(tag);
    }

    @Transactional
    public void mergeTags(Long sourceId, Long targetId) {
        Tag sourceTag = tagRepository.findById(sourceId)
            .orElseThrow(() -> new BusinessException("源标签不存在"));
        Tag targetTag = tagRepository.findById(targetId)
            .orElseThrow(() -> new BusinessException("目标标签不存在"));

        tagRepository.delete(sourceTag);
    }
}
