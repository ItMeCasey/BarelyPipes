#!/usr/bin/env python3
"""Tests for plan_matrix.py. Run from the repo root: python -I -m unittest discover -s .github/scripts
or: python -I .github/scripts/test_plan_matrix.py

Synthetic file lists always run; the cases built from real commits are skipped when the commit is not in the clone.
"""
import json
import os
import subprocess
import sys
import tempfile
import unittest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import plan_matrix as pm  # noqa: E402

ALL = set(pm.NAMES)
MC262 = {"forge-26.2", "neoforge-26.2"}
MC263 = {"forge-26.3", "neoforge-26.3"}


def git_files(*args):
    """Paths of a real commit range, or None when git or the commit is not available."""
    try:
        out = subprocess.run(["git", "diff", "--name-only", "--no-renames", *args], capture_output=True, text=True, cwd=pm.ROOT)
    except OSError:
        return None
    return out.stdout.split() if out.returncode == 0 else None


class SyntheticBuild(unittest.TestCase):
    def pick(self, *files):
        return set(pm.pick_build(list(files)))

    def test_docs_and_status_pick_nothing(self):
        self.assertEqual(self.pick("PROJECT_STATUS.md", "README.md", "CHANGELOG.md", "docs/frames/viewers/x.png",
                                   "branding/banners/banner.png", "curseforge/DESCRIPTION.md", "LICENSE", ".gitignore",
                                   "dist/x.jar", "logs/a.log", "release/x"), set())

    def test_workflow_and_ci_script_changes_pick_nothing(self):
        self.assertEqual(self.pick(".github/workflows/build.yml", ".github/workflows/release.yml",
                                   ".github/scripts/plan_matrix.py", ".github/scripts/publish_curseforge.py",
                                   ".github/ISSUE_TEMPLATE/config.yml"), set())

    def test_common_picks_everything(self):
        self.assertEqual(self.pick("common/src/main/java/com/itmecasey/barelypipes/A.java"), ALL)
        self.assertEqual(self.pick("common/src/main/resources/assets/barelypipes/lang/en_us.json"), ALL)
        self.assertEqual(self.pick("common/src/test/java/com/itmecasey/barelypipes/ATest.java"), ALL)

    def test_single_module_change_picks_exactly_that_module(self):
        self.assertEqual(self.pick("forge-26.3/src/main/java/x/GhostSlot.java"), {"forge-26.3"})
        self.assertEqual(self.pick("neoforge-1.21.1/gradle.properties"), {"neoforge-1.21.1"})
        self.assertEqual(self.pick("forge-26.1.2/src/dev/java/x/SelfTest.java"), {"forge-26.1.2"})

    def test_module_build_and_run_folders_pick_nothing(self):
        self.assertEqual(self.pick("forge-26.2/build/libs/x.jar", "neoforge-26.2/run/options.txt", "neoforge-26.2/runs/a"), set())

    def test_libs_pick_the_minecraft_version_they_belong_to(self):
        self.assertEqual(self.pick("libs/smwti-api-26.2.jar"), MC262)
        self.assertEqual(self.pick("libs/barelystorage-mc26.3.jar"), MC263)
        self.assertEqual(self.pick("libs/smwti-api-1.20.1.jar"), {"forge-1.20.1", "neoforge-1.20.1"})
        self.assertEqual(self.pick("libs/new-thing.jar"), ALL)

    def test_self_test_script_picks_everything_but_generators_pick_nothing(self):
        self.assertEqual(self.pick("scripts/server_check.py"), ALL)
        self.assertEqual(self.pick("scripts/make_assets.py", "scripts/gen_plug_arrows.py", "scripts/templates/ViewerTour-26.2.java.tmpl",
                                   "scripts/add_viewers.py", "scripts/build_all.sh"), set())
        self.assertEqual(self.pick("scripts/brand_new_tool.py"), ALL)

    def test_unknown_path_picks_everything(self):
        self.assertEqual(self.pick("something/new.txt"), ALL)
        self.assertEqual(self.pick("build.gradle"), ALL)
        self.assertEqual(self.pick(".gitattributes"), ALL)
        self.assertEqual(self.pick("forge-9.9/build.gradle"), ALL)

    def test_mixed_change_is_the_union(self):
        self.assertEqual(self.pick("PROJECT_STATUS.md", "forge-26.2/gradle.properties", "neoforge-26.3/gradle.properties"), {"forge-26.2", "neoforge-26.3"})

    def test_backslash_and_dot_slash_paths(self):
        self.assertEqual(self.pick("forge-26.2\\src\\main\\java\\A.java"), {"forge-26.2"})
        self.assertEqual(self.pick("./.github/workflows/build.yml"), set())

    def test_diff_failure_picks_everything(self):
        self.assertEqual(set(pm.pick_build(None)), ALL)
        self.assertIsNone(pm.changed_files("0123456789abcdef0123456789abcdef01234567", "fedcba9876543210fedcba9876543210fedcba98"))

    def test_outputs(self):
        out = pm.outputs(["forge-26.2"])
        self.assertEqual((out["any"], out["count"]), ("true", "1"))
        self.assertEqual(json.loads(out["matrix"]), {"include": [{"layer": "forge-26.2", "java": 25}]})
        nothing = pm.outputs([])
        self.assertEqual((nothing["any"], nothing["count"], nothing["matrix"]), ("false", "0", '{"include": []}'))
        self.assertEqual(json.loads(pm.outputs(pm.NAMES)["matrix"])["include"][0], {"layer": "neoforge-1.20.1", "java": 17})


class RealCommits(unittest.TestCase):
    def check(self, a, b, expected):
        files = git_files(a, b)
        if files is None:
            self.skipTest(f"commit {a} or {b} is not in this clone")
        self.assertEqual(set(pm.pick_build(files)), expected)

    def test_status_only_commit(self):
        self.check("5723c0e^", "5723c0e", set())

    def test_docs_only_commit(self):
        self.check("53c2bdb^", "53c2bdb", set())

    def test_one_minecraft_version_commit(self):
        self.check("f658f52^", "f658f52", MC263)

    def test_common_change_commit(self):
        self.check("af1d9ee^", "af1d9ee", ALL)

    def test_common_textures_commit(self):
        self.check("d7604ad^", "d7604ad", ALL)


class Release(unittest.TestCase):
    def make_root(self, versions):
        root = tempfile.mkdtemp()
        for module in pm.NAMES:
            os.makedirs(os.path.join(root, module))
            with open(os.path.join(root, module, "gradle.properties"), "w", encoding="utf-8") as fh:
                fh.write(f"mod_id=barelypipes\nmod_version={versions.get(module, '2026.11.4')}\n")
        return root

    def test_only_modules_at_the_tag_version(self):
        root = self.make_root({m: "2026.11.5" for m in MC263})
        picked, version = pm.pick_release("v2026.11.5", root)
        self.assertEqual(version, "2026.11.5")
        self.assertEqual(set(picked), MC263)
        self.assertEqual(set(pm.pick_release("v2026.11.4", root)[0]), ALL - MC263)

    def test_all_modules_at_the_tag_version(self):
        root = self.make_root({m: "2026.11.5" for m in pm.NAMES})
        self.assertEqual(set(pm.pick_release("v2026.11.5", root)[0]), ALL)

    def test_tag_nobody_carries_picks_nothing(self):
        self.assertEqual(pm.pick_release("v2026.11.9", self.make_root({}))[0], [])

    def test_manual_run_uses_the_newest_version(self):
        root = self.make_root({m: "2026.11.10" for m in MC262})
        picked, version = pm.pick_release("main", root)
        self.assertEqual((version, set(picked)), ("2026.11.10", MC262))

    def test_real_repo_versions(self):
        version = pm.newest_version()
        if not version:
            self.skipTest("no mod_version found")
        picked, _ = pm.pick_release("v" + version)
        self.assertTrue(picked)
        for module in picked:
            self.assertEqual(pm.module_version(module), version)


if __name__ == "__main__":
    unittest.main()
