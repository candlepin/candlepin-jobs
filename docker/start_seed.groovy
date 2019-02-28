def job = hudson.model.Hudson.instance.getItemByFullName('Development Seed Job')
hudson.model.Hudson.instance.queue.schedule(job, 0)