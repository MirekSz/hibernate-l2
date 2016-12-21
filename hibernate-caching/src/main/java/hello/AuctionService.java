package hello;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuctionService {
	@Autowired
	AuctionRepo repo;
	@PersistenceContext
	EntityManager em;

	public void add1500Auctions() {
		for (int i = 0; i < 1600; i++) {
			repo.save(new Auction("Dysk SSD", "Dysk SSD", BigDecimal.TEN));
		}
	}

	public void cacheCollection(int threadId) {
		Auction find = em.find(Auction.class, Long.valueOf(threadId));
		find.toString();
		System.out.println("set");
		int size = find.getItemSet().size();
		System.out.println("set size " + size);

		System.out.println("query");
		Query query = em.createNamedQuery("itemsForAuciton");
		query.setParameter("id", Long.valueOf(threadId));
		query.setHint("org.hibernate.cacheable", true);
		List resultList = query.getResultList();
		System.out.println("hql size " + resultList.size());

		Item item = new Item();
		item.setAuction(find);
		item.setName("sad");
		em.persist(item);
	}

	public void cache(int threadId) {
		List<Long> findAll = repo.findAllAucitonsIds(new PageRequest(threadId, 100));
		for (Long id : findAll) {
			Auction auction = repo.findOne(id);
			auction.toString();
			if (id % 5 == 0) {
				auction.setDescription(System.nanoTime() + "");
			}
		}

		for (Long id : repo.findTop10(new PageRequest(0, 20))) {
			Auction auction = repo.findOne(id);
			auction.toString();
			auction.setDescription(System.nanoTime() + "");
		}
	}

}
