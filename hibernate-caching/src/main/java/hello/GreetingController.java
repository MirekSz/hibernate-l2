package hello;

import java.lang.reflect.Field;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;

import org.hibernate.SessionFactory;
import org.hibernate.cache.infinispan.InfinispanRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.internal.CacheImpl;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.TransactionController;

@RestController
public class GreetingController {
	@Autowired
	AuctionService service;

	@Autowired
	EntityManagerFactory emf;

	private CacheManager eCM;

	private EmbeddedCacheManager iCM;

	public CacheManager ehCacheManager() {
		SessionFactory unwrap = emf.unwrap(SessionFactory.class);
		org.hibernate.internal.CacheImpl cache = (CacheImpl) unwrap.getCache();
		RegionFactory regionFactory = cache.getRegionFactory();
		try {
			Field declaredField = Class.forName("org.hibernate.cache.ehcache.AbstractEhcacheRegionFactory")
					.getDeclaredField("manager");
			declaredField.setAccessible(true);
			CacheManager object = (CacheManager) declaredField.get(regionFactory);
			return object;
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		// return new
		// EhcacheTransactionManager(ehcacheManager().getTransactionController());
		return null;
	}

	private void inCacheTx(Runnable runnable) throws Exception {
		if (eCM != null) {
			TransactionController controller = eCM.getTransactionController();
			controller.begin();
			try {
				runnable.run();
				controller.commit();
			} catch (Exception e) {
				e.printStackTrace();
				controller.rollback();
			}
		} else if (iCM != null) {
			// Set<String> cacheNames = iCM.getCacheNames();
			// for (String string : cacheNames) {
			// System.out.println(string);
			// }
			TransactionManager transactionManager = iCM.getCache().getAdvancedCache().getTransactionManager();
			transactionManager.begin();
			try {
				runnable.run();
				transactionManager.commit();
			} catch (Exception e) {
				e.printStackTrace();
				transactionManager.rollback();
			}
		}

	}

	@PostConstruct
	public void init() throws Exception {
		this.eCM = ehCacheManager();
		this.iCM = infinispanCacheManager();
		inCacheTx(service::add1500Auctions);
	}

	private EmbeddedCacheManager infinispanCacheManager() {
		SessionFactory unwrap = emf.unwrap(SessionFactory.class);
		org.hibernate.internal.CacheImpl cache = (CacheImpl) unwrap.getCache();
		RegionFactory regionFactory = cache.getRegionFactory();
		if (regionFactory instanceof InfinispanRegionFactory) {
			InfinispanRegionFactory reg = (InfinispanRegionFactory) regionFactory;
			EmbeddedCacheManager cacheManager = reg.getCacheManager();
			return cacheManager;
		}
		return null;
	}

	@RequestMapping("/action")
	@ResponseStatus(code = HttpStatus.OK)
	public void action(@RequestParam("id") Long id) throws Exception {
		inCacheTx(() -> service.cache(id.intValue()));
	}
}
