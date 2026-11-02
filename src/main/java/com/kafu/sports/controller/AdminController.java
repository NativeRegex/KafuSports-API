package com.kafu.sports.controller;

import com.kafu.sports.entity.*;
import com.kafu.sports.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final TeamRepository teamRepo;
    private final FixtureRepository fixtureRepo;
    private final ResultRepository resultRepo;
    private final PlayerRepository playerRepo;

    public AdminController(TeamRepository teamRepo, FixtureRepository fixtureRepo,
                           ResultRepository resultRepo, PlayerRepository playerRepo) {
        this.teamRepo = teamRepo;
        this.fixtureRepo = fixtureRepo;
        this.resultRepo = resultRepo;
        this.playerRepo = playerRepo;
    }

    // ---------- TEAMS ----------
    @PostMapping("/teams")
    public Team createTeam(@RequestBody Map<String, String> body) {
        Team t = new Team();
        t.setName(body.get("name"));
        t.setSport(body.getOrDefault("sport", "Men's Football"));
        return teamRepo.save(t);
    }

    @DeleteMapping("/teams/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id) {
        teamRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }

    // ---------- FIXTURES ----------
    @PostMapping("/fixtures")
    public Fixture createFixture(@RequestBody Map<String, Object> body) {
        Fixture f = new Fixture();
        f.setHomeTeam(teamRepo.findById(Long.valueOf(body.get("homeTeamId").toString())).orElseThrow());
        f.setAwayTeam(teamRepo.findById(Long.valueOf(body.get("awayTeamId").toString())).orElseThrow());
        f.setMatchDate(LocalDateTime.parse(body.get("matchDate").toString()));
        f.setVenue(body.get("venue").toString());
        f.setStatus("SCHEDULED");
        return fixtureRepo.save(f);
    }

    @DeleteMapping("/fixtures/{id}")
    public ResponseEntity<?> deleteFixture(@PathVariable Long id) {
        fixtureRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }

    // ---------- RESULTS ----------
    @PostMapping("/results")
    public Result createResult(@RequestBody Map<String, Object> body) {
        Fixture f = fixtureRepo.findById(Long.valueOf(body.get("fixtureId").toString())).orElseThrow();
        int homeScore = Integer.parseInt(body.get("homeScore").toString());
        int awayScore = Integer.parseInt(body.get("awayScore").toString());

        Result r = new Result();
        r.setHomeTeam(f.getHomeTeam());
        r.setAwayTeam(f.getAwayTeam());
        r.setHomeScore(homeScore);
        r.setAwayScore(awayScore);
        r.setMatchDate(f.getMatchDate());
        r.setVenue(f.getVenue());
        resultRepo.save(r);

        Team home = f.getHomeTeam();
        Team away = f.getAwayTeam();

        home.setGoalsFor(home.getGoalsFor() + homeScore);
        home.setGoalsAgainst(home.getGoalsAgainst() + awayScore);
        away.setGoalsFor(away.getGoalsFor() + awayScore);
        away.setGoalsAgainst(away.getGoalsAgainst() + homeScore);

        if (homeScore > awayScore) {
            home.setWins(home.getWins() + 1); home.setPoints(home.getPoints() + 3);
            away.setLosses(away.getLosses() + 1);
        } else if (homeScore < awayScore) {
            away.setWins(away.getWins() + 1); away.setPoints(away.getPoints() + 3);
            home.setLosses(home.getLosses() + 1);
        } else {
            home.setDraws(home.getDraws() + 1); home.setPoints(home.getPoints() + 1);
            away.setDraws(away.getDraws() + 1); away.setPoints(away.getPoints() + 1);
        }
        teamRepo.save(home);
        teamRepo.save(away);

        f.setStatus("COMPLETED");
        f.setHomeScore(homeScore);
        f.setAwayScore(awayScore);
        fixtureRepo.save(f);

        return r;
    }

    @DeleteMapping("/results/{id}")
    public ResponseEntity<?> deleteResult(@PathVariable Long id) {
        resultRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }

    // ---------- PLAYERS ----------
    @PostMapping("/players")
    public Player createPlayer(@RequestBody Map<String, Object> body) {
        Player p = new Player();
        p.setName(body.get("name").toString());
        p.setPosition(body.get("position").toString());
        p.setJerseyNumber(Integer.parseInt(body.get("jerseyNumber").toString()));
        p.setTeam(teamRepo.findById(Long.valueOf(body.get("teamId").toString())).orElseThrow());
        return playerRepo.save(p);
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable Long id) {
        playerRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }
}
