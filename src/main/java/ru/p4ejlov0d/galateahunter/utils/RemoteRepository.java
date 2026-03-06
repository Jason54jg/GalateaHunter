package ru.p4ejlov0d.galateahunter.utils;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import ru.p4ejlov0d.galateahunter.utils.config.ModConfigHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static ru.p4ejlov0d.galateahunter.GalateaHunter.LOGGER;
import static ru.p4ejlov0d.galateahunter.GalateaHunter.MOD_ID;
import static ru.p4ejlov0d.galateahunter.repo.impl.ShardRepoImpl.imagesRootPath;

public class RemoteRepository {
    private static RemoteRepository instance;

    private boolean isNeedUpdate = false;

    private RemoteRepository() {
    }

    public static RemoteRepository getInstance() {
        return instance == null ? instance = new RemoteRepository() : instance;
    }

    public CompletableFuture<Void> checkForUpdates() {
        int oldImagesCount = ModConfigHolder.getConfig().imagesCount;

        CompletableFuture<Git> git = CompletableFuture.supplyAsync(() -> {
            try {
                return Git.open(imagesRootPath.toFile());
            } catch (Exception e) {
                LOGGER.info("Failed to open repository at path: {}, cause {}", imagesRootPath, e.getMessage());
                return null;
            }
        });
        CompletableFuture<Repository> repos = git.thenApply(Git::getRepository);
        CompletableFuture<ObjectId> id = git.thenApply(g -> {
            if (g == null) return null;

            try {
                return g.fetch()
                        .setInitialBranch("refs/heads/master")
                        .setDepth(1)
                        .setRefSpecs("refs/heads/master:refs/remotes/origin/master")
                        .call();
            } catch (Exception e) {
                LOGGER.warn("Failed to fetch remote repository", e);
                return null;
            }
        }).thenApply(result -> result.getTrackingRefUpdate("refs/remotes/origin/master").getNewObjectId());

        return repos.thenCombine(id, (repo, objectId) -> {
            int count = 0;

            try (RevWalk walk = new RevWalk(repo)) {
                RevCommit commit = walk.parseCommit(objectId);

                try (TreeWalk treeWalk = new TreeWalk(repo)) {
                    treeWalk.reset(commit.getTree());
                    treeWalk.setRecursive(false);

                    while (treeWalk.next()) {
                        if (treeWalk.getPathString().equals("public") || treeWalk.getPathString().equals("public/shardIcons"))
                            treeWalk.enterSubtree();
                        if (treeWalk.getPathString().startsWith("public/shardIcons/")) count++;
                    }
                }
            } catch (Exception e) {
                count = -1;
                LOGGER.warn("Failed to read object in repository", e);
            }

            if (count != oldImagesCount) {
                isNeedUpdate = true;
                LOGGER.info("It seems the remote repository has been updated");
            }

            return repo;
        }).thenCombine(git, (repo, g) -> {
            repo.close();
            g.close();
            return null;
        });
    }

    public void cloneRepoWithImages() {
        try {
            FileUtils.cleanDirectory(imagesRootPath.toFile());
        } catch (FileNotFoundException e) {
            LOGGER.info("Failed to clean directory {}, cause {}", imagesRootPath.toAbsolutePath(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Failed to clean directory {}", imagesRootPath.toAbsolutePath(), e);
            return;
        }

        try {
            LOGGER.info("Cloning skyshards repository to {}", imagesRootPath.toAbsolutePath());

            Git.cloneRepository()
                    .setURI("https://github.com/Campionnn/SkyShards.git")
                    .setDirectory(imagesRootPath.toFile())
                    .setBranchesToClone(List.of("refs/heads/master"))
                    .setBranch("master")
                    .setDepth(1)
                    .call()
                    .close();

            final File tempGit = imagesRootPath.getParent().resolve(".git/").toFile();
            final File tempDir = imagesRootPath.getParent().resolve("assets/" + MOD_ID + "/").toFile();

            FileUtils.copyDirectoryToDirectory(imagesRootPath.resolve(".git/").toFile(), tempGit.getParentFile());
            FileUtils.copyToDirectory(Arrays.asList(imagesRootPath.resolve("public/shardIcons").toFile().listFiles()), tempDir);

            FileUtils.cleanDirectory(imagesRootPath.toFile());

            for (File file : tempDir.listFiles()) {
                file.renameTo(new File(file.getParentFile(), file.getName().toLowerCase()));
            }

            ModConfigHolder.getConfig().imagesCount = tempDir.listFiles().length;
            ModConfigHolder.save();
            isNeedUpdate = false;

            FileUtils.moveDirectoryToDirectory(tempDir.getParentFile(), imagesRootPath.toFile(), false);
            FileUtils.moveDirectoryToDirectory(tempGit, imagesRootPath.toFile(), false);

            LOGGER.info("Successfully cloned skyshards repository to {}", imagesRootPath.toAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Failed to clone repository to {}", imagesRootPath.toAbsolutePath(), e);
            try {
                FileUtils.deleteDirectory(imagesRootPath.toFile());
            } catch (IOException ex) {
                LOGGER.error("Failed to rollback cloning repository to {}", imagesRootPath.toAbsolutePath(), ex);
            }
        }
    }

    public boolean isNeedUpdate() {
        return isNeedUpdate;
    }
}
