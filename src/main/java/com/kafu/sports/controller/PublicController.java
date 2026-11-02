package com.kafu.sports.controller;

import com.kafu.sports.entity.Fixture;
import com.kafu.sports.entity.Player;
import com.kafu.sports.entity.Result;
import com.kafu.sports.entity.Team;
import com.kafu.sports.repository.FixtureRepository;
import com.kafu.sports.repository.PlayerRepository;
import com.kafu.sports.repository.ResultRepository;
import com.kafu.sports.repository.TeamRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PublicController {

    private final TeamRepository teamRepo;
    private final FixtureRepository fixtureRepo;
    private final ResultRepository resultRepo;
    private final PlayerRepository playerRepo;

    public PublicController(TeamRepository teamRepo, FixtureRepository fixtureRepo,
                            ResultRepository resultRepo, PlayerRepository playerRepo) {
        this.teamRepo = teamRepo;
        this.fixtureRepo = fixtureRepo;
        this.resultRepo = resultRepo;
        this.playerRepo = playerRepo;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "kafu-sports-api");
    }

    // ---- LEAGUE TABLE ----
    @GetMapping("/league")
    public List<Map<String, Object>> league() {
        List<Team> teams = teamRepo.findAll();
        teams.sort((a, b) -> {
            int cmp = Integer.compare(b.getPoints(), a.getPoints());
            if (cmp != 0) return cmp;
            return Integer.compare(b.getGoalDifference(), a.getGoalDifference());
        });

        List<Map<String, Object>> out = new ArrayList<>();
        int pos = 1;
        for (Team t : teams) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("pos", pos++);
            row.put("team", Map.of("id", t.getId(), "name", t.getName()));
            row.put("played", t.getPlayed());
            row.put("wins", t.getWins());
            row.put("draws", t.getDraws());
            row.put("losses", t.getLosses());
            row.put("goalsFor", t.getGoalsFor());
            row.put("goalsAgainst", t.getGoalsAgainst());
            row.put("goalDifference", t.getGoalDifference());
            row.put("points", t.getPoints());
            out.add(row);
        }
        return out;
    }

    // ---- TEAMS ----
    @GetMapping("/teams")
    public List<Team> teams() {
        return teamRepo.findAll();
    }

    @GetMapping("/teams/{id}")
    public Map<String, Object> teamDetail(@PathVariable Long id) {
        Team team = teamRepo.findById(id).orElseThrow();

        List<Fixture> teamFixtures = fixtureRepo.findAll().stream()
                .filter(f -> f.getHomeTeam().getId().equals(id) || f.getAwayTeam().getId().equals(id))
                .filter(f -> "SCHEDULED".equals(f.getStatus()))
                .toList();

        List<Result> teamResults = resultRepo.findAll().stream()
                .filter(r -> r.getHomeTeam().getId().equals(id) || r.getAwayTeam().getId().equals(id))
                .toList();

        List<Player> squad = playerRepo.findByTeamId(id);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", team.getId());
        out.put("name", team.getName());
        out.put("sport", team.getSport());
        out.put("played", team.getPlayed());
        out.put("wins", team.getWins());
        out.put("draws", team.getDraws());
        out.put("losses", team.getLosses());
        out.put("goalsFor", team.getGoalsFor());
        out.put("goalsAgainst", team.getGoalsAgainst());
        out.put("goalDifference", team.getGoalDifference());
        out.put("points", team.getPoints());
        out.put("squad", squad);
        out.put("fixtures", teamFixtures);
        out.put("results", teamResults);
        return out;
    }

    // ---- FIXTURES ----
    @GetMapping("/fixtures")
    public List<Map<String, Object>> fixtures() {
        return fixtureRepo.findAll().stream().map(f -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("homeTeam", Map.of("id", f.getHomeTeam().getId(), "name", f.getHomeTeam().getName()));
            m.put("awayTeam", Map.of("id", f.getAwayTeam().getId(), "name", f.getAwayTeam().getName()));
            m.put("matchDate", f.getMatchDate());
            m.put("venue", f.getVenue());
            m.put("status", f.getStatus());
            m.put("homeScore", f.getHomeScore());
            m.put("awayScore", f.getAwayScore());
            return m;
        }).collect(Collectors.toList());
    }

    // ---- RESULTS ----
    @GetMapping("/results")
    public List<Map<String, Object>> results() {
        return resultRepo.findAll().stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("homeTeam", Map.of("id", r.getHomeTeam().getId(), "name", r.getHomeTeam().getName()));
            m.put("awayTeam", Map.of("id", r.getAwayTeam().getId(), "name", r.getAwayTeam().getName()));
            m.put("homeScore", r.getHomeScore());
            m.put("awayScore", r.getAwayScore());
            m.put("matchDate", r.getMatchDate());
            m.put("venue", r.getVenue());
            return m;
        }).collect(Collectors.toList());
    }

    // ---- PLAYERS ----
    @GetMapping("/players")
    public List<Player> players() {
        return playerRepo.findAllByOrderByGoalsDesc();
    }

    // ---- STATS ----
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<Result> allResults = resultRepo.findAll();
        int totalGoals = allResults.stream().mapToInt(r -> r.getHomeScore() + r.getAwayScore()).sum();

        return Map.of(
                "totalGoals", totalGoals,
                "totalMatches", allResults.size(),
                "totalTeams", teamRepo.count(),
                "topScorers", playerRepo.findAllByOrderByGoalsDesc().stream().limit(10).toList()
        );
    }
}
