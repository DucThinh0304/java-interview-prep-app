# Changelog

All notable changes to this project will be documented in this file.

- Initialized multi-module project skeleton (:core-model, :core-engine, :content-java, :app) with Hilt, Room, Compose, dev theme tokens, and unit tests.
- Designed kotlinx.serialization content schema in :core-model (Pack, Unit, Note, 8 Question kinds), added PackValidator with unit tests, and created validated sample pack in :content-java.
- Removed the legacy Exercise/Lesson/ContentPack model, its old questions.json/lessons.json/manifest.json assets, and the unused Difficulty.JUNIOR/MID/SENIOR aliases, now that :core-engine and :app are fully migrated to the Pack/Question/Note schema.
