package org.vomzersocials.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.vomzersocials.user.services.interfaces.FollowerService;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowerService followerService;

    public FollowController(FollowerService followerService) {
        this.followerService = followerService;
    }

    @PostMapping
    public ResponseEntity<?> follow(@RequestParam String followerId, @RequestParam String followingId) {
        followerService.followUser(followerId, followingId);
        return ResponseEntity.ok("Followed successfully");
    }
}
