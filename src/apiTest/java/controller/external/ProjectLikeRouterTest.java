package controller.external;

import constant.ProjectState;
import exception.AlreadyExistException;
import exception.NotFoundException;
import fixture.ProjectFixtures;
import model.domain.AppUser;
import model.domain.Project;
import model.domain.ProjectLike;
import model.dto.request.like.LikeRequest;
import model.dto.response.DeleteResponse;
import model.dto.response.IdResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repository.ProjectLikeRepository;
import repository.ProjectRepository;
import service.external.ProjectLikeService;
import support.ApiEndpoint;
import support.ApiResponse;
import support.ApiTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectLikeRouter (/project/like)")
class ProjectLikeRouterTest extends ApiTest {

    private Project project() {
        return ProjectFixtures.project(createUser(), ProjectState.IN_PROGRESS);
    }

    private Project projectWithLikes(long likeCount, long dislikeCount) {
        return ProjectFixtures.projectWithLikes(createUser(), ProjectState.IN_PROGRESS, likeCount, dislikeCount);
    }

    private Project findProject(long projectId) {
        return bean(ProjectRepository.class).findById(projectId).orElseThrow();
    }

    private List<ProjectLike> likesOf(long projectId, long userId) {
        return bean(ProjectLikeRepository.class).findByProjectAndUser(projectId, userId);
    }

    private static Map<String, Object> likeBody(boolean likeType) {
        return Map.of("likeType", likeType);
    }

    private static LikeRequest likeRequest(Boolean likeType) {
        return LikeRequest.builder().likeType(likeType).build();
    }

    // ===================================================================== POST /project/like/{project_id}

    @Nested
    @ApiEndpoint(method = "POST", path = "/project/like/{project_id}")
    @DisplayName("POST /project/like/{project_id} - 프로젝트 좋아요/싫어요 등록")
    class CreateProjectLike {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("좋아요를 누르면 200과 id를 돌려주고 likeCount가 1 오른다")
            void like() {
                Project saved = project();
                AppUser user = createUser();

                ApiResponse response = client.post("/project/like/" + saved.getId()).bearer(login(user))
                        .json(likeBody(true)).send().assertStatus(200);

                long likeId = response.json("/id").asLong();
                ProjectLike like = bean(ProjectLikeRepository.class).findById(likeId).orElseThrow();
                assertEquals(saved.getId(), like.getProject().getId());
                assertEquals(user.getId(), like.getUser().getId());
                assertTrue(like.getLikeType());

                ApiResponse project = client.get("/project/" + saved.getId()).send().assertStatus(200);
                assertEquals(1L, project.json("/data/likeCount").asLong(), project::toString);
                assertEquals(0L, project.json("/data/dislikeCount").asLong(), project::toString);
            }

            @Test
            @DisplayName("싫어요를 누르면 dislikeCount가 1 오른다")
            void dislike() {
                Project saved = project();
                AppUser user = createUser();

                client.post("/project/like/" + saved.getId()).bearer(login(user))
                        .json(likeBody(false)).send().assertStatus(200);

                Project after = findProject(saved.getId());
                assertEquals(0L, after.getLikeCount());
                assertEquals(1L, after.getDislikeCount());
                assertFalse(likesOf(saved.getId(), user.getId()).getFirst().getLikeType());
            }

            @Test
            @DisplayName("같은 유저가 같은 프로젝트에 다시 누르면 409이고 카운트는 그대로다")
            void duplicate() {
                Project saved = project();
                AppUser user = createUser();
                String token = login(user);
                client.post("/project/like/" + saved.getId()).bearer(token).json(likeBody(true)).send().assertStatus(200);

                client.post("/project/like/" + saved.getId()).bearer(token).json(likeBody(true)).send().assertStatus(409);

                assertEquals(1L, findProject(saved.getId()).getLikeCount());
                assertEquals(1, likesOf(saved.getId(), user.getId()).size());
            }

            @Test
            @DisplayName("없는 프로젝트면 404")
            void notFound() {
                client.post("/project/like/" + Long.MAX_VALUE).bearer(login(createUser()))
                        .json(likeBody(true)).send().assertStatus(404);
            }

            @Test
            @DisplayName("토큰이 없으면 401이고 카운트는 그대로다")
            void noToken() {
                Project saved = project();

                client.post("/project/like/" + saved.getId()).json(likeBody(true)).send().assertStatus(401);

                assertEquals(0L, findProject(saved.getId()).getLikeCount());
            }

            @Test
            @DisplayName("잘못된 토큰이면 401")
            void invalidToken() {
                client.post("/project/like/" + project().getId()).bearer("a.b.c")
                        .json(likeBody(true)).send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                client.post("/project/like/" + project().getId()).bearer(login(createUser())).send().assertStatus(422);
            }

            @Test
            @DisplayName("likeType이 불리언이 아니면 422")
            void invalidLikeType() {
                client.post("/project/like/" + project().getId()).bearer(login(createUser()))
                        .rawJson("{\"likeType\": \"maybe\"}").send().assertStatus(422);
            }

            // likeType이 null이면 updateLikeValue의 `likeType ? ...` 언박싱에서 NPE가 나 500이 된다
            @Test
            @DisplayName("[BUG] likeType이 빠지면 4xx이고 저장되지 않는다")
            void missingLikeType() {
                Project saved = project();
                AppUser user = createUser();

                ApiResponse response = client.post("/project/like/" + saved.getId()).bearer(login(user))
                        .rawJson("{}").send();

                assertTrue(response.status() >= 400 && response.status() < 500, response::toString);
                assertTrue(likesOf(saved.getId(), user.getId()).isEmpty());
            }

            @Test
            @DisplayName("project_id가 숫자가 아니면 422")
            void invalidPathType() {
                client.post("/project/like/abc").bearer(login(createUser()))
                        .json(likeBody(true)).send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("좋아요는 likeCount만 1 올리고 좋아요 행을 만든다")
            void like() {
                Project saved = projectWithLikes(3, 2);
                AppUser user = createUser();

                IdResponse result = bean(ProjectLikeService.class)
                        .createProjectLike(saved.getId(), user.getId(), likeRequest(true));

                Project after = findProject(saved.getId());
                assertEquals(4L, after.getLikeCount());
                assertEquals(2L, after.getDislikeCount());
                ProjectLike like = bean(ProjectLikeRepository.class).findById(result.getId()).orElseThrow();
                assertTrue(like.getLikeType());
                assertEquals(user.getId(), like.getUser().getId());
            }

            @Test
            @DisplayName("싫어요는 dislikeCount만 1 올린다")
            void dislike() {
                Project saved = projectWithLikes(3, 2);
                AppUser user = createUser();

                bean(ProjectLikeService.class).createProjectLike(saved.getId(), user.getId(), likeRequest(false));

                Project after = findProject(saved.getId());
                assertEquals(3L, after.getLikeCount());
                assertEquals(3L, after.getDislikeCount());
            }

            @Test
            @DisplayName("여러 유저가 누르면 그만큼 오른다")
            void multipleUsers() {
                Project saved = project();

                bean(ProjectLikeService.class).createProjectLike(saved.getId(), createUser().getId(), likeRequest(true));
                bean(ProjectLikeService.class).createProjectLike(saved.getId(), createUser().getId(), likeRequest(true));

                assertEquals(2L, findProject(saved.getId()).getLikeCount());
                assertEquals(2, bean(ProjectLikeRepository.class).findByProject(saved.getId()).size());
            }

            @Test
            @DisplayName("없는 프로젝트면 NotFoundException")
            void unknownProject() {
                AppUser user = createUser();

                assertThrows(NotFoundException.class, () -> bean(ProjectLikeService.class)
                        .createProjectLike(Long.MAX_VALUE, user.getId(), likeRequest(true)));
            }

            @Test
            @DisplayName("없는 유저면 NotFoundException이고 카운트는 그대로다")
            void unknownUser() {
                Project saved = project();

                assertThrows(NotFoundException.class, () -> bean(ProjectLikeService.class)
                        .createProjectLike(saved.getId(), Long.MAX_VALUE, likeRequest(true)));
                assertEquals(0L, findProject(saved.getId()).getLikeCount());
            }

            // unique(project, user) 위반은 SQLExecutor에서 AlreadyExistException으로 바뀌고 트랜잭션이 롤백된다
            @Test
            @DisplayName("이미 누른 프로젝트에 다시 누르면 AlreadyExistException이고 카운트는 그대로다")
            void duplicate() {
                Project saved = project();
                AppUser user = createUser();
                bean(ProjectLikeService.class).createProjectLike(saved.getId(), user.getId(), likeRequest(true));

                assertThrows(AlreadyExistException.class, () -> bean(ProjectLikeService.class)
                        .createProjectLike(saved.getId(), user.getId(), likeRequest(false)));
                Project after = findProject(saved.getId());
                assertEquals(1L, after.getLikeCount());
                assertEquals(0L, after.getDislikeCount());
            }
        }
    }

    // ===================================================================== PATCH /project/like/{project_id}

    @Nested
    @ApiEndpoint(method = "PATCH", path = "/project/like/{project_id}")
    @DisplayName("PATCH /project/like/{project_id} - 프로젝트 좋아요/싫어요 변경")
    class UpdateProjectLike {

        @Nested
        @DisplayName("Http")
        class Http {
            // editProjectLike가 projectLikeRepository.update(project.getId(), ...)로 좋아요 id 대신 프로젝트 id를 넘긴다
            @Test
            @DisplayName("[BUG] 좋아요를 싫어요로 바꾸면 200과 좋아요 id를 돌려주고 행과 카운트가 바뀐다")
            void likeToDislike() {
                Project saved = projectWithLikes(1, 0);
                AppUser user = createUser();
                ProjectLike like = ProjectFixtures.like(saved, user, true);

                ApiResponse response = client.patch("/project/like/" + saved.getId()).bearer(login(user))
                        .json(likeBody(false)).send().assertStatus(200);

                assertEquals(like.getId(), response.json("/id").asLong(), response::toString);
                assertFalse(bean(ProjectLikeRepository.class).findById(like.getId()).orElseThrow().getLikeType());
                Project after = findProject(saved.getId());
                assertEquals(0L, after.getLikeCount());
                assertEquals(1L, after.getDislikeCount());
            }

            // editProjectLike가 findByProjectAndUser(...).getFirst()를 빈 목록에 불러 NoSuchElementException(500)이 난다
            @Test
            @DisplayName("[BUG] 누른 적이 없으면 404")
            void noLike() {
                client.patch("/project/like/" + project().getId()).bearer(login(createUser()))
                        .json(likeBody(false)).send().assertStatus(404);
            }

            @Test
            @DisplayName("토큰이 없으면 401")
            void noToken() {
                client.patch("/project/like/" + project().getId()).json(likeBody(false)).send().assertStatus(401);
            }

            @Test
            @DisplayName("body가 없으면 422")
            void missingBody() {
                Project saved = projectWithLikes(1, 0);
                AppUser user = createUser();
                ProjectFixtures.like(saved, user, true);

                client.patch("/project/like/" + saved.getId()).bearer(login(user)).send().assertStatus(422);

                assertEquals(1L, findProject(saved.getId()).getLikeCount());
            }

            @Test
            @DisplayName("project_id가 숫자가 아니면 422")
            void invalidPathType() {
                client.patch("/project/like/abc").bearer(login(createUser())).json(likeBody(true)).send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            // editProjectLike가 좋아요 행 대신 id가 프로젝트 id와 같은 좋아요 행을 수정한다
            @Test
            @DisplayName("[BUG] 싫어요를 좋아요로 바꾸면 dislikeCount가 내려가고 likeCount가 오르며 행이 바뀐다")
            void dislikeToLike() {
                Project saved = projectWithLikes(2, 3);
                AppUser user = createUser();
                ProjectLike like = ProjectFixtures.like(saved, user, false);

                IdResponse result = bean(ProjectLikeService.class)
                        .editProjectLike(saved.getId(), user.getId(), likeRequest(true));

                assertEquals(like.getId(), result.getId());
                assertTrue(bean(ProjectLikeRepository.class).findById(like.getId()).orElseThrow().getLikeType());
                Project after = findProject(saved.getId());
                assertEquals(3L, after.getLikeCount());
                assertEquals(2L, after.getDislikeCount());
            }

            // editProjectLike가 프로젝트 id로 좋아요 행을 찾아 수정하려다 없는 행이면 NotFoundException으로 롤백된다
            @Test
            @DisplayName("[BUG] 같은 값으로 바꾸면 카운트는 그대로이고 좋아요 id를 돌려준다")
            void sameType() {
                Project saved = projectWithLikes(5, 1);
                AppUser user = createUser();
                ProjectLike like = ProjectFixtures.like(saved, user, true);

                IdResponse result = bean(ProjectLikeService.class)
                        .editProjectLike(saved.getId(), user.getId(), likeRequest(true));

                assertEquals(like.getId(), result.getId());
                Project after = findProject(saved.getId());
                assertEquals(5L, after.getLikeCount());
                assertEquals(1L, after.getDislikeCount());
            }

            // 빈 목록에 getFirst()를 불러 NotFoundException 대신 NoSuchElementException이 난다
            @Test
            @DisplayName("[BUG] 누른 적이 없으면 NotFoundException")
            void noLike() {
                Project saved = project();
                AppUser user = createUser();

                assertThrows(NotFoundException.class, () -> bean(ProjectLikeService.class)
                        .editProjectLike(saved.getId(), user.getId(), likeRequest(true)));
            }
        }
    }

    // ===================================================================== DELETE /project/like/{project_id}

    @Nested
    @ApiEndpoint(method = "DELETE", path = "/project/like/{project_id}")
    @DisplayName("DELETE /project/like/{project_id} - 프로젝트 좋아요/싫어요 취소")
    class DeleteProjectLike {

        @Nested
        @DisplayName("Http")
        class Http {
            @Test
            @DisplayName("취소하면 200과 result=true, 행이 지워지고 likeCount가 1 내려간다")
            void deletes() {
                Project saved = projectWithLikes(1, 0);
                AppUser user = createUser();
                ProjectLike like = ProjectFixtures.like(saved, user, true);

                ApiResponse response = client.delete("/project/like/" + saved.getId()).bearer(login(user))
                        .send().assertStatus(200);

                assertTrue(response.json("/result").asBoolean(), response::toString);
                assertTrue(bean(ProjectLikeRepository.class).findById(like.getId()).isEmpty());
                ApiResponse project = client.get("/project/" + saved.getId()).send().assertStatus(200);
                assertEquals(0L, project.json("/data/likeCount").asLong(), project::toString);
            }

            @Test
            @DisplayName("POST로 누른 좋아요를 DELETE로 취소하면 원래 카운트로 돌아간다")
            void roundTrip() {
                Project saved = projectWithLikes(4, 4);
                String token = login(createUser());

                client.post("/project/like/" + saved.getId()).bearer(token).json(likeBody(false)).send().assertStatus(200);
                assertEquals(5L, findProject(saved.getId()).getDislikeCount());

                client.delete("/project/like/" + saved.getId()).bearer(token).send().assertStatus(200);
                Project after = findProject(saved.getId());
                assertEquals(4L, after.getLikeCount());
                assertEquals(4L, after.getDislikeCount());
            }

            // deleteProjectLike가 빈 목록에 getFirst()를 불러 NoSuchElementException(500)이 난다
            @Test
            @DisplayName("[BUG] 누른 적이 없으면 404")
            void noLike() {
                client.delete("/project/like/" + project().getId()).bearer(login(createUser())).send().assertStatus(404);
            }

            @Test
            @DisplayName("토큰이 없으면 401이고 행은 남아 있다")
            void noToken() {
                Project saved = projectWithLikes(1, 0);
                ProjectLike like = ProjectFixtures.like(saved, createUser(), true);

                client.delete("/project/like/" + saved.getId()).send().assertStatus(401);

                assertTrue(bean(ProjectLikeRepository.class).findById(like.getId()).isPresent());
            }

            @Test
            @DisplayName("project_id가 숫자가 아니면 422")
            void invalidPathType() {
                client.delete("/project/like/abc").bearer(login(createUser())).send().assertStatus(422);
            }
        }

        @Nested
        @DisplayName("Logic")
        class Logic {
            @Test
            @DisplayName("싫어요를 취소하면 dislikeCount만 1 내려간다")
            void deletesDislike() {
                Project saved = projectWithLikes(2, 2);
                AppUser user = createUser();
                ProjectFixtures.like(saved, user, false);

                DeleteResponse result = bean(ProjectLikeService.class).deleteProjectLike(saved.getId(), user.getId());

                assertTrue(result.getResult());
                Project after = findProject(saved.getId());
                assertEquals(2L, after.getLikeCount());
                assertEquals(1L, after.getDislikeCount());
                assertTrue(likesOf(saved.getId(), user.getId()).isEmpty());
            }

            @Test
            @DisplayName("다른 유저의 좋아요는 지우지 않는다")
            void keepsOtherUsers() {
                Project saved = projectWithLikes(2, 0);
                AppUser me = createUser();
                AppUser other = createUser();
                ProjectFixtures.like(saved, me, true);
                ProjectLike othersLike = ProjectFixtures.like(saved, other, true);

                bean(ProjectLikeService.class).deleteProjectLike(saved.getId(), me.getId());

                assertTrue(bean(ProjectLikeRepository.class).findById(othersLike.getId()).isPresent());
                assertEquals(1L, findProject(saved.getId()).getLikeCount());
            }

            @Test
            @DisplayName("카운트는 0 아래로 내려가지 않는다")
            void notBelowZero() {
                Project saved = projectWithLikes(0, 0);
                AppUser user = createUser();
                ProjectFixtures.like(saved, user, true);

                bean(ProjectLikeService.class).deleteProjectLike(saved.getId(), user.getId());

                assertEquals(0L, findProject(saved.getId()).getLikeCount());
            }

            // 빈 목록에 getFirst()를 불러 NotFoundException 대신 NoSuchElementException이 난다
            @Test
            @DisplayName("[BUG] 누른 적이 없으면 NotFoundException")
            void noLike() {
                Project saved = project();
                AppUser user = createUser();

                assertThrows(NotFoundException.class,
                        () -> bean(ProjectLikeService.class).deleteProjectLike(saved.getId(), user.getId()));
            }
        }
    }
}
