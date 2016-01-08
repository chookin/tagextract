package cmri.tagbase.orm.repository;

import cmri.tagbase.orm.domain.TagMapEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Created by zhuyin on 4/15/15.
 * http://docs.spring.io/spring-data/jpa/docs/1.4.3.RELEASE/reference/html/jpa.repositories.html
 */
@Transactional(readOnly = true)
public interface TagMapRepository extends CrudRepository<TagMapEntity, Integer> {
    List<TagMapEntity> findBySite(String site);

    @Modifying
    void deleteBySite(String site);

    @Modifying
    void deleteBySiteIn(Collection<String> sites);

    @Query("select distinct site from TagMapEntity")
    List<String> findDistinctSite();
}