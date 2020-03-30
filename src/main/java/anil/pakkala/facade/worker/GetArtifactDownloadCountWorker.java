package anil.pakkala.facade.worker;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import anil.pakkala.facade.artifactory.ArtifactoryClient;
import anil.pakkala.facade.client.exception.ArtifactoryClientException;
import anil.pakkala.facade.client.exception.WorkerException;
import anil.pakkala.facade.artifactory.model.Artifact;
import anil.pakkala.facade.artifactory.model.ArtifactStats;
import anil.pakkala.facade.worker.dto.ArtifactDownloadCount;

/**
 * Task responsible for get the download count of an artifact 
 * @author anil pakkala
 *
 */
public class GetArtifactDownloadCountWorker implements Callable<ArtifactDownloadCount>{
    
    private static final Logger logger = Logger.getLogger(GetArtifactDownloadCountWorker.class.getName());

    private ArtifactoryClient artifactoryClient;
    private Artifact artifact;
    
    public GetArtifactDownloadCountWorker(ArtifactoryClient artifactoryClient, Artifact artifact) {
        super();
        this.artifactoryClient = artifactoryClient;
        this.artifact = artifact;
    }
    
    @Override
    public ArtifactDownloadCount call() {
        
        try {
            ArtifactStats stats = artifactoryClient.getArtifactStats(artifact);
            return new ArtifactDownloadCount(artifact, stats.getDownloadCount());
            
        } catch (ArtifactoryClientException e) {
            throw new WorkerException("Failed to get Artifact download count", e);
        }
    }

}
