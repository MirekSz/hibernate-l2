package hello;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuctionService {
	@Autowired
	AuctionRepo repo;

	public void add1500Auctions() {
		for (int i = 0; i < 1600; i++) {
			repo.save(new Auction("Dysk SSD", "Dysk SSD", BigDecimal.TEN));
		}
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
