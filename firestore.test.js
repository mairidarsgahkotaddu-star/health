import test from 'node:test';
import assert from 'node:assert';
import fs from 'node:fs';

test('firestore.rules file exists and contains valid structure', () => {
  const content = fs.readFileSync('firestore.rules', 'utf8');
  assert.ok(content.includes("rules_version = '2';"), 'Must declare rules_version');
  assert.ok(content.includes('service cloud.firestore'), 'Must declare service');
  assert.ok(content.includes('match /databases/{database}/documents'), 'Must match root documents');
  assert.ok(content.includes('isOwner(userId)'), 'Must enforce isOwner check');
  assert.ok(content.includes('isValidCheckup('), 'Must validate checkup');
  assert.ok(content.includes('isValidMedication('), 'Must validate medication');
  assert.ok(content.includes('isValidTest('), 'Must validate tests');
  assert.ok(content.includes('isValidVital('), 'Must validate vitals');
});
