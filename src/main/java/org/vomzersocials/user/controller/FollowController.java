package org.vomzersocials.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vomzersocials.user.dtos.requests.FollowUserRequest;
import org.vomzersocials.user.services.interfaces.FollowerService;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    @Autowired
    private final FollowerService followerService;

    public FollowController(FollowerService followerService) {
        this.followerService = followerService;
    }

    @PostMapping("/toggle")
    public ResponseEntity<String> toggleFollower(@RequestBody FollowUserRequest followUserRequest) {
        followerService.toggleFollow(followUserRequest);
        return ResponseEntity.ok("Follow status updated");
    }
    @PostMapping("/follow")
    public ResponseEntity<String> follow(@RequestParam FollowUserRequest followUserRequest) {
        followerService.followUser(followUserRequest);
        return ResponseEntity.ok("Followed successfully");
    }
    @PostMapping("/unfollow")
    public ResponseEntity<String> unfollow(@RequestParam FollowUserRequest followUserRequest) {
        followerService.unfollowUser(followUserRequest);
        return ResponseEntity.ok("Unfollowed successfully");
    }
}
