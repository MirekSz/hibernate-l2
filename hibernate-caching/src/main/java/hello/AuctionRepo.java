package hello;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepo extends JpaRepository<Auction, Long> {
	@Query("FROM Auction a WHERE a.creator = ?#{@company.getCompany()}")
	@QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
	List<Auction> getAllForCurrentUser();

	@Query("SELECT a.id FROM Auction a ORDER BY a.id")
	List<Long> findAllAucitonsIds(Pageable pageable);

	@Query("SELECT a.id FROM Auction a ORDER BY a.id")
	List<Long> findTop10(Pageable pageable);

	@Override
	// @Yours
	@QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
	Auction findOne(Long id);

	@QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
	List<Auction> findTop10ByActiveIsTrue();
}
