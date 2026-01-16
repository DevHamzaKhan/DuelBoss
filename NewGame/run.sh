#!/bin/bash
javac -d . util/*.java entity/*.java particle/*.java enemy/*.java ability/*.java manager/*.java ui/*.java core/*.java&&java -cp . core.Game
