package anil.pakkala.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import anil.pakkala.facade.artifactory.ArtifactoryClient;
import anil.pakkala.facade.client.exception.ArtifactoryClientException;
import anil.pakkala.facade.artifactory.model.Artifact;
import anil.pakkala.facade.artifactory.utils.ArtifactDownloadCountRanking;
import anil.pakkala.facade.client.exception.RestClientException;
import anil.pakkala.facade.worker.GetMostPopularArtifactsWorker;
import anil.pakkala.facade.worker.dto.ArtifactDownloadCount;



public class ArtifactoryFacade {
	
	private static final Logger logger = Logger.getLogger(ArtifactoryFacade.class.getName());
	
	private ArtifactoryClient client;

	public ArtifactoryFacade(ArtifactoryClient client) {
		super();
		this.client = client;
	}
	
	
	public List<ArtifactDownloadCount> getMostPopularJar(String repository, int rankingSize, int maxConcurrentWorkers,
			int threadsPerWorker, Integer itemsPerWorker) throws ArtifactoryClientException {

		List<Future<List<ArtifactDownloadCount>>> workersFuture = new ArrayList<>();
		ExecutorService workersExecutor = Executors.newFixedThreadPool(maxConcurrentWorkers);
		ArtifactDownloadCountRanking ranking = new ArtifactDownloadCountRanking(rankingSize);

		// query the artifactory based on type jar
		String aql = getListItemsFromRepoByNameQuery(repository, "*.jar");

		// Set number of items per page
		int pageSize = 0;
		if (itemsPerWorker != null && itemsPerWorker > 0) {
			pageSize = itemsPerWorker;
		} else {
			try {
				pageSize = client.getQueryResultsLimit();
			} catch (ArtifactoryClientException e) {
				if (e.getCause() instanceof RestClientException
						&& ((RestClientException) e.getCause()).getStatusCode() == 403) {
					throw new ArtifactoryClientException(
							"Failed to get page size because auth token does not have admin privileges. Inform a value through parameters.",
							e);
				} else {
					throw new ArtifactoryClientException("Failed to get page size: " + e.getMessage(), e);
				}
			}
		}

		try {
			// Set current page result to page size so the query is performed at least one
			// time
			int currentPageResults = pageSize;
			int currentPage = 0;

			// Query pages until the number of results is lesser than page size
			System.out.println("currentPageResults are " + currentPageResults);
			System.out.println("pageSize is " + pageSize);
			while (currentPageResults == pageSize) {
				List<Artifact> pageResults = client.queryItemsPage(aql, currentPage * pageSize, pageSize);
				currentPageResults = pageResults.size();
				currentPage++;

				// Submit results to processing
				if (!pageResults.isEmpty()) {
					GetMostPopularArtifactsWorker worker = new GetMostPopularArtifactsWorker(currentPage, client,
							pageResults, rankingSize, threadsPerWorker);
					workersFuture.add(workersExecutor.submit(worker));
				}
			}

			// updating ranking
			for (int i = 0; i < workersFuture.size(); i++) {
				List<ArtifactDownloadCount> workerRanking = workersFuture.get(i).get();
				ranking.updateRanking(workerRanking);
			}

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ArtifactoryClientException("Execution interrupted for Artifactory Client");
		} catch (ExecutionException e) {
			throw new ArtifactoryClientException("Failed to get nth Artifactory popular jars", e);
		} finally {
			workersExecutor.shutdownNow();
		}

		return ranking.getRanking();
	}

	
	 public String getListItemsFromRepoByNameQuery(String repository, String name) {

	        //  query configuring
	        StringBuilder aql = new StringBuilder();
	        aql.append("items.find({");
	        aql.append("\"repo\":{\"$eq\":\"").append(repository).append("\"},");
	        aql.append("\"name\":{\"$match\":\"").append(name).append("\"}");
	        aql.append("})");
	        return aql.toString();
	    }
	
	
}
