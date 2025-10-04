import jenkins.model.*
import hudson.security.*
import jenkins.security.s2m.AdminWhitelistRule

def j = Jenkins.get()

def adminUser = System.getenv("ADMIN_USER") ?: "admin"
def adminPass = System.getenv("ADMIN_PASS") ?: "admin123"
def ciUser    = System.getenv("CI_USER")    ?: "ci"
def ciPass    = System.getenv("CI_PASS")    ?: "ci123"

def realm = new HudsonPrivateSecurityRealm(false)
if (realm.getUser(adminUser) == null) { realm.createAccount(adminUser, adminPass) }
if (realm.getUser(ciUser) == null)    { realm.createAccount(ciUser, ciPass) }
j.setSecurityRealm(realm)

def strategy = new GlobalMatrixAuthorizationStrategy()
strategy.add(Jenkins.ADMINISTER, adminUser)
[
  Jenkins.READ,
  Item.READ, Item.DISCOVER, Item.BUILD, Item.WORKSPACE,
  Run.READ, View.READ
].each { p -> strategy.add(p, ciUser) }
// No permissions for 'anonymous'
j.setAuthorizationStrategy(strategy)

j.injector.getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false)
j.save()
println "[init.groovy] Security configured: anonymous disabled; admin + ci users created."
